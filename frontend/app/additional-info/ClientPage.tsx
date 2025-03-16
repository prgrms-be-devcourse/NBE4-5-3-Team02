'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { motion, AnimatePresence } from 'framer-motion';
import { PhoneIcon, MapPinIcon, DocumentMagnifyingGlassIcon, ExclamationTriangleIcon } from '@heroicons/react/24/solid';
import { AddressData } from "@/types/d";
import {CheckCircleIcon} from "lucide-react";

export default function ClientPage() {
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
    const [isGeoLoading, setIsGeoLoading] = useState(true);

    const BASE_URL = process.env.NEXT_PUBLIC_BASE_URL;

    // 브라우저 위치 정보 조회
    useEffect(() => {
        console.log('위치 정보 조회 시작');

        if (!navigator.geolocation) {
            console.error('브라우저가 Geolocation을 지원하지 않음');
            setGeoError('Geolocation is not supported by your browser');
            setIsGeoLoading(false); // 로드 완료
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (position) => {
                console.log('위치 정보 수신 성공:', position);
                setFormData(prev => ({
                    ...prev,
                    latitude: position.coords.latitude,
                    longitude: position.coords.longitude
                }));
                setIsGeoLoading(false); // 로드 완료
            },
            (err) => {
                console.error('위치 정보 오류 발생:', err);
                setGeoError('Unable to retrieve your location: ' + err.message);
                setIsGeoLoading(false); // 로드 완료
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
            } // @ts-expect-error: 'open' 메서드에서 타입 오류 발생 가능성 있음
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

        if (isGeoLoading) {
            setError('📍 위치 정보를 가져오는 중입니다. 잠시만 기다려주세요...');
            return;
        }

        if (!formData.latitude || !formData.longitude) {
            setError('위치 정보를 가져올 수 없습니다.');
            return;
        }

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
            const response = await fetch(`${BASE_URL}/oauth/users/additional-info`, {
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

            if (!response.ok || data.code === "400-1") {
                throw {
                    type: 'LOCATION_ERROR',
                    message: '위치 정보 오류',
                    details: {
                        allowedRadius: 5,
                    }
                };
            }

            // 성공 처리
            sessionStorage.removeItem('requiresAdditionalInfo');
            alert('가입을 환영합니다! 추가 정보가 성공적으로 저장되었습니다.');
            router.push('/');
        } catch (err) {
            if (isCustomError(err)) {
                if (err.type === 'LOCATION_ERROR') {
                    console.log(err);
                    setError(`🗺️ 지역 제한 서비스 안내
• 현재 위치에서 5km 이내 지역만 서비스 제공`);
                } else {
                    setError(`⚠️ ${err.message}`);
                }
            } else if (err instanceof Error) {
                // 일반적인 Error 객체 처리
                setError(`⚠️ ${err.message}`);
            } else {
                // 알 수 없는 에러 처리
                setError('⚠️ 알 수 없는 오류가 발생했습니다.');
                console.error(err);
            }
        } finally {
            setIsLoading(false);
        }
    };

    // 커스텀 에러 타입 가드 함수
    function isCustomError(error: unknown): error is { type: string; message: string; details?: any } {
        return typeof error === 'object' && error !== null && 'type' in error && 'message' in error;
    }

    return (
        <div className="min-h-screen flex flex-col justify-center py-12 sm:px-6 lg:px-8">
            <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                className="max-w-md w-full mx-auto bg-white p-8 rounded-lg shadow-md"
            >
                <h1 className="text-2xl font-bold mb-6 flex items-center text-gray-800">
                    <DocumentMagnifyingGlassIcon className="h-8 w-8 text-green-600 mr-2" />
                    추가 정보 입력
                </h1>

                <AnimatePresence>
                    {(error || geoError) && (
                        <motion.div
                            initial={{ opacity: 0, y: -20 }}
                            animate={{ opacity: 1, y: 0 }}
                            exit={{ opacity: 0, y: -20 }}
                            className="mb-4 p-4 bg-red-50 border-l-4 border-red-400 rounded-lg"
                        >
                            <div className="flex items-start"> {/* 왼쪽 정렬을 위한 items-start */}
                                <ExclamationTriangleIcon className="h-6 w-6 text-red-500 mt-1 mr-2" />
                                <div className="space-y-2 text-left"> {/* 텍스트 왼쪽 정렬 */}
                                    <pre className="text-sm text-red-700 whitespace-pre-wrap">
            {error}
          </pre>
                                </div>
                            </div>
                        </motion.div>
                    )}
                </AnimatePresence>

                <form onSubmit={handleSubmit} className="space-y-6">
                    {/* 전화번호 입력 필드 */}
                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                            전화번호
                        </label>
                        <div className="relative rounded-md shadow-sm">
                            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                <PhoneIcon className="h-5 w-5 text-gray-500" />
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
                        <div className="relative">
                            <label htmlFor="postalCode" className="block text-sm font-medium text-gray-700 mb-1">
                                우편번호 검색
                                <span className="text-xs text-gray-500 ml-1">(검색 버튼 클릭)</span>
                            </label>
                            <div className="flex gap-2 relative">
                                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                    <MapPinIcon className="h-5 w-5 text-gray-500" />
                                </div>
                                <input
                                    type="text"
                                    id="postalCode"
                                    name="postalCode"
                                    value={formData.postalCode}
                                    readOnly
                                    placeholder="주소 검색을 시작하려면 돋보기 버튼을 클릭하세요"
                                    className="block w-full pl-10 pr-3 py-2.5 border border-gray-300 rounded-md
      focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500
      sm:text-sm text-gray-700 placeholder-gray-400 bg-white"
                                    onClick={handleAddressSearch}
                                />
                                <motion.button
                                    type="button"
                                    onClick={handleAddressSearch}
                                    whileHover={{ scale: 1.05 }}
                                    whileTap={{ scale: 0.95 }}
                                    className="p-2 bg-green-600 text-white rounded-md hover:bg-green-700
          transition-colors duration-200"
                                    aria-label="주소 검색"
                                >
                                    <DocumentMagnifyingGlassIcon className="h-5 w-5" />
                                </motion.button>
                            </div>
                        </div>

                        {/* 실시간 상태 표시기 */}
                        {formData.postalCode && (
                            <motion.div
                                initial={{ opacity: 0, y: -5 }}
                                animate={{ opacity: 1, y: 0 }}
                                className="text-xs text-green-600 mt-1 flex items-center"
                            >
                                <CheckCircleIcon className="h-4 w-4 mr-1" />
                                유효한 우편번호가 입력되었습니다
                            </motion.div>
                        )}
                    </div>

                    {/* 주소 필드 자동 채우기 */}
                    {formData.baseAddress && (
                        <motion.div
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            className="space-y-2"
                        >
                            <div>
                                <label className="block text-sm font-medium text-gray-700">
                                    기본 주소
                                </label>
                                <input
                                    type="text"
                                    name="baseAddress"
                                    value={formData.baseAddress}
                                    readOnly
                                    className="block w-full pl-4 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm text-gray-700 placeholder-gray-400"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700">
                                    상세 주소
                                </label>
                                <input
                                    type="text"
                                    name="detailAddress"
                                    placeholder="동/호수 정보를 입력해주세요 (예: 101동 202호)"
                                    className="block w-full pl-4 pr-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm text-gray-700 placeholder-gray-400"
                                    onChange={handleChange}
                                />
                            </div>
                        </motion.div>
                    )}

                    <button
                        type="submit"
                        onClick={handleSubmit}
                        disabled={isLoading}
                        className="w-full bg-green-500 hover:bg-green-600 text-white font-medium py-3 rounded-lg transition-all duration-300 flex items-center justify-center"
                    >
                        {isLoading ? (
                            <motion.div
                                animate={{ rotate: 360 }}
                                transition={{ duration: 1, repeat: Infinity }}
                                className="h-5 w-5 border-2 border-white rounded-full border-t-transparent"
                            />
                        ) : (
                            '🚀 정보 저장하기'
                        )}
                    </button>

                    {/* 위치 기반 서비스 설명 툴팁 */}
                    <div className="mt-4 p-3 bg-blue-50 rounded-lg border border-blue-200">
                        <div className="flex items-start">
                            <MapPinIcon className="h-5 w-5 text-green-500 mt-1 mr-2" />
                            <div>
                                <h3 className="font-medium text-green-800">📍 위치 기반 서비스 안내</h3>
                                <p className="text-sm text-green-600 mt-1">
                                    당사는 사용자의 정확한 위치 정보를 기반으로 맞춤형 서비스를 제공합니다.
                                </p>
                                <ul className="list-disc list-inside text-green-600 text-sm mt-2 pl-2">
                                    <li>실시간 지역별 예약 가능 안내</li>
                                    <li>근처 예약 가능 서비스 제공</li>
                                    <li>지역 커뮤니티 연동</li>
                                </ul>
                            </div>
                        </div>
                    </div>

                </form>
            </motion.div>
        </div>
    );
}