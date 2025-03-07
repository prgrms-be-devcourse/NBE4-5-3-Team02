'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { motion } from 'framer-motion';
import { PhoneIcon, MapPinIcon, DocumentMagnifyingGlassIcon } from '@heroicons/react/24/solid';
import { AddressData } from "@/types/d";

export default function AdditionalInfoPage() {
    const router = useRouter();
    const [formData, setFormData] = useState({
        phoneNumber: '',
        postalCode: '',
        baseAddress: '',
        detailAddress: '',
        latitude: 0,
        longitude: 0
    });

    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const [geoError, setGeoError] = useState('');

    // 브라우저 위치 정보 조회
    useEffect(() => {
        console.log('위치 정보 조회 시작');

        if (!navigator.geolocation) {
            console.error('브라우저가 Geolocation을 지원하지 않음');
            setGeoError('Geolocation is not supported by your browser');
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (position) => {
                console.log('위치 정보 수신 성공:', position);
                console.log('위도:', position.coords.latitude);
                console.log('경도:', position.coords.longitude);

                setFormData(prev => ({
                    ...prev,
                    latitude: position.coords.latitude,
                    longitude: position.coords.longitude
                }));

                // 상태 업데이트 확인
                setTimeout(() => {
                    console.log('업데이트된 formData:', {
                        lat: position.coords.latitude,
                        lng: position.coords.longitude
                    });
                }, 0);
            },
            (err) => {
                console.error('위치 정보 오류 발생:', err);
                console.log('에러 코드:', err.code);
                console.log('에러 메시지:', err.message);
                setGeoError('Unable to retrieve your location: ' + err.message);
            }
        );
    }, []);

    // 카카오 주소 검색 핸들러
    const handleAddressSearch = () => {

        if (!window.daum) {
            console.error('카카오 API가 로드되지 않았습니다');
            return;
        }

        new window.daum.Postcode({
            oncomplete: (data: AddressData) => {
                console.log('선택된 주소 데이터:', data);
                setFormData(prev => ({
                    ...prev,
                    postalCode: data.zonecode,
                    baseAddress: `${data.address} ${data.buildingName || ''}`.trim()
                }));
            },
            onresize: (size) => {
                window.resizeTo(size.width, size.height);
            }
        }).open({
            popupTitle: '주소 검색',
            popupKey: 'kakaoPopup'
        });
    };

    // 입력 핸들러 타입 명시화
    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    // 세션 확인
    useEffect(() => {
        const requiresAdditionalInfo = sessionStorage.getItem('requiresAdditionalInfo');
        if (!requiresAdditionalInfo) {
            router.replace('/');
        }
    }, [router]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!formData.latitude || !formData.longitude) {
            setError('위치 정보를 가져오는 중입니다. 잠시 후 다시 시도해주세요.');
            return;
        }

        setIsLoading(true);
        setError('');

        // 제출 데이터 확인 로그 추가
        console.log('최종 제출 데이터:', {
            ...formData,
            latitude: formData.latitude,
            longitude: formData.longitude
        });

        try {
            const response = await fetch('http://localhost:8080/oauth/users/additional-info', {
                method: 'PATCH',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    phoneNumber: formData.phoneNumber,
                    postalCode: formData.postalCode,
                    baseAddress: formData.baseAddress,
                    detailAddress: formData.detailAddress,
                    latitude: formData.latitude,
                    longitude: formData.longitude
                }),
            });

            const data = await response.json();
            if (!response.ok) throw new Error(data.message || '정보 저장 실패');

            sessionStorage.removeItem('requiresAdditionalInfo');
            router.push('/');
        } catch (err) {
            setError(err instanceof Error ? err.message : '알 수 없는 오류 발생');
        } finally {
            setIsLoading(false);
        }
    };
    return (
        <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
            <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                className="max-w-md w-full mx-auto bg-white p-8 rounded-lg shadow-md"
            >
                <h2 className="text-center text-3xl font-extrabold text-gray-900 mb-8">
                    📝 추가 정보 입력
                </h2>

                {geoError && (
                    <div className="mb-4 p-3 bg-red-100 text-red-700 rounded-md">
                        {geoError}
                    </div>
                )}

                <form onSubmit={handleSubmit} className="space-y-6">
                    {/* 전화번호 입력 필드 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            전화번호
                        </label>
                        <div className="relative rounded-md shadow-sm">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <PhoneIcon className="h-5 w-5 text-gray-400" />
                            </div>
                            <input
                                type="tel"
                                name="phoneNumber"
                                required
                                value={formData.phoneNumber}
                                onChange={handleChange}
                                className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm text-gray-700 placeholder-gray-400"
                                placeholder="01012345678"
                            />
                        </div>
                    </div>

                    {/* 주소 입력 필드 */}
                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                우편번호
                            </label>
                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    name="postalCode"
                                    readOnly
                                    value={formData.postalCode}
                                    className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md bg-gray-100 cursor-pointer text-gray-700 placeholder-gray-500"
                                    onClick={handleAddressSearch}
                                    placeholder="우편번호 검색"
                                />
                                <button
                                    type="button"
                                    onClick={handleAddressSearch}
                                    className="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 flex items-center"
                                >
                                    <DocumentMagnifyingGlassIcon className="h-5 w-5 mr-2" />
                                    검색
                                </button>
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                기본 주소
                            </label>
                            <div className="relative rounded-md shadow-sm">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center">
                                    <MapPinIcon className="h-5 w-5 text-gray-400" />
                                </div>
                                <input
                                    type="text"
                                    name="baseAddress"
                                    readOnly
                                    value={formData.baseAddress}
                                    className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md bg-gray-100 text-gray-700"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">
                                상세 주소
                            </label>
                            <input
                                type="text"
                                name="detailAddress"
                                value={formData.detailAddress}
                                onChange={handleChange}
                                className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:ring-green-500 focus:border-green-500 text-gray-700 placeholder-gray-400"
                                placeholder="상세 주소 입력"
                            />
                        </div>
                    </div>

                    {error && (
                        <div className="text-red-600 text-sm text-center mt-2">{error}</div>
                    )}

                    <button
                        type="submit"
                        disabled={isLoading || !!geoError}
                        className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:opacity-50"
                    >
                        {isLoading ? '저장 중...' : '정보 저장'}
                    </button>
                </form>
            </motion.div>
        </div>
    );
}