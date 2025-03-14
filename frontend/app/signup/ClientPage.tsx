'use client';

import {useState, useEffect, useRef} from 'react';
import {useRouter} from 'next/navigation';
import {motion, AnimatePresence} from 'framer-motion';
import {
    EnvelopeIcon,
    MapPinIcon,
    UserCircleIcon,
    LockClosedIcon,
    DevicePhoneMobileIcon,
    SparklesIcon,
    IdentificationIcon,
    PaperAirplaneIcon,
    MagnifyingGlassIcon,
    ExclamationTriangleIcon,
    ClockIcon
} from '@heroicons/react/24/outline';
import {ArrowRightIcon, CheckCircleIcon} from "lucide-react";

type FormData = {
    username: string;
    password: string;
    checkPassword: string;
    email: string;
    nickname: string;
    phoneNumber: string;
    postalCode: string;
    baseAddress: string;
    detailAddress: string;
    latitude: number;
    longitude: number;
    verificationCode: string;
};

export default function SignupPage() {
    const router = useRouter();
    const [currentStep, setCurrentStep] = useState(1);
    const [countdown, setCountdown] = useState<number>(0);
    const [, setIsLoading] = useState(false);
    const [isEmailVerified, setIsEmailVerified] = useState(false);
    const intervalRef = useRef<NodeJS.Timeout>();
    const [error, setError] = useState('');
    const [formData, setFormData] = useState<FormData>({
        username: '',
        password: '',
        checkPassword: '',
        email: '',
        nickname: '',
        phoneNumber: '',
        postalCode: '',
        baseAddress: '',
        detailAddress: '',
        latitude: 0,
        longitude: 0,
        verificationCode: ''
    });

    const [errors, setErrors] = useState<Record<string, string>>({});
    const [, setGeoError] = useState('');
    const BASE_URL = process.env.NEXT_PUBLIC_BASE_URL;

    // 카운트다운 로직
    useEffect(() => {
        if (countdown > 0) {
            intervalRef.current = setInterval(() => {
                setCountdown((prev) => {
                    if (prev <= 1) {
                        clearInterval(intervalRef.current!);
                        return 0;
                    }
                    return prev - 1;
                });
            }, 1000);
        }
        return () => {
            if (intervalRef.current) {
                clearInterval(intervalRef.current);
            }
        };
    }, [countdown]);

    // 위치 정보 조회
    useEffect(() => {
        if (!navigator.geolocation) {
            setGeoError('브라우저가 위치 서비스를 지원하지 않습니다');
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (position) => {
                setFormData(prev => ({
                    ...prev,
                    latitude: position.coords.latitude,
                    longitude: position.coords.longitude
                }));
            },
            (err) => {
                setGeoError(`위치 정보 오류: ${err.message}`);
            }
        );
    }, []);

    // 카카오 주소 검색
    const handleAddressSearch = () => {
        if (!window.daum) {
            console.error('카카오 API 로드 실패');
            return;
        }

        new window.daum.Postcode({
            oncomplete: (data) => {
                setFormData(prev => ({
                    ...prev,
                    postalCode: data.zonecode,
                    baseAddress: `${data.address} ${data.buildingName || ''}`.trim()
                }));
            }
            // @ts-expect-error: 'open' 메서드에서 타입 오류 발생 가능성 있음
        }).open();
    };

    // 이메일 인증 요청
    const handleSendVerificationCode = async () => {
        try {
            const response = await fetch(`${BASE_URL}/api/v1/users/send-verification-code`, {
                method: 'POST',
                credentials: 'include',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({email: formData.email})
            });

            if (!response.ok) throw new Error('인증 코드 전송 실패');
            setCountdown(900); // 15분 타이머 시작
            if (intervalRef.current) {
                clearInterval(intervalRef.current);
            }
        } catch (err) {
            console.error('Error sending verification code:', err); // 에러 로그 출력
            setErrors({ email: '인증 코드 전송에 실패했습니다' });
        }
    };

    // 인증 코드 확인
    const handleVerifyEmail = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/v1/users/verified-email', {
                method: 'POST',
                credentials: 'include',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    email: formData.email,
                    code: formData.verificationCode
                })
            });

            if (!response.ok) throw new Error('인증 실패');
            setIsEmailVerified(true);
            setTimeout(() => {
                setCurrentStep(prev => Math.min(prev + 1, 3)); // 3은 최대 단계 수
            }, 1000);
        } catch (err) {
            console.error('Error verifying email:', err); // 에러 로그 출력
            setErrors(prev => ({ ...prev, verification: '인증 코드가 일치하지 않습니다' }));
        }
    };

    // 회원 가입 제출
    //@ts-expect-error: React 에러 가능성 있음
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);

        const validationErrors = validateForm();
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        try {
            const response = await fetch('http://localhost:8080/api/v1/users/signup', {
                method: 'POST',
                credentials: 'include',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(formData)
            });

            const result = await response.json();

            if (!response.ok || result.code === "400-1") {
                throw {
                    type: 'LOCATION_ERROR',
                    message: '위치 정보 오류',
                    details: {
                        allowedRadius: 5,
                    }
                };
            }

            if (result.code === '201-2') {
                alert('🎉 회원 가입이 완료되었습니다! 로그인 페이지로 이동합니다.');
                router.push('/login');
            }

        } catch (err : any) {
            if (err.type === 'LOCATION_ERROR') {
            setError(`🗺️ 지역 제한 서비스 안내
• 현재 위치에서 5km 이내 지역만 서비스 제공`);
        } else {
            setError(`⚠️ ${err.message}`);
        }
        } finally {
            setIsLoading(false);
        }
    };

    const validateForm = () => {
        const newErrors: { [key: string]: string } = {};
        if (!formData.email.includes('@')) newErrors.email = '유효한 이메일을 입력해주세요';
        if (formData.password.length < 8) newErrors.password = '비밀번호는 영문+숫자 조합 8자 이상이어야 합니다';
        if (formData.verificationCode.length !== 8)
            newErrors.verificationCode = '인증 코드는 8글자입니다';
        if (formData.username.length > 20 || formData.username.length < 8) newErrors.username = '사용자 ID는 8~20자로 입력해 주세요';
        return newErrors;
    };

    return (
        <div className="max-w-2xl mx-auto p-6 space-y-8">
            {/* 단계 표시기 */}
            <div className="flex items-center justify-center relative mb-12">
                <div className="flex items-center justify-between w-64">
                    {[1, 2, 3].map((step) => (
                        <div key={step} className="relative z-10">
                            <div
                                className={`w-10 h-10 rounded-full flex items-center justify-center text-white 
                                ${currentStep >= step ? 'bg-green-500' : 'bg-gray-300'}`}
                            >
                                {step}
                            </div>
                        </div>
                    ))}
                </div>
                <div className="absolute top-1/2 left-16 right-16 h-1 bg-gray-200 transform -translate-y-1/2"></div>
            </div>

            <motion.form
                onSubmit={handleSubmit}
                className="space-y-6"
                initial={{opacity: 0}}
                animate={{opacity: 1}}
                transition={{duration: 0.5}}
            >

                {/* 1단계: 이메일 인증 */}

                <AnimatePresence mode='wait'>
                    {currentStep === 1 && (
                        <motion.div
                            key="step1"
                            initial={{opacity: 0, x: 50}}
                            animate={{opacity: 1, x: 0}}
                            exit={{opacity: 0, x: -50}}
                            className="space-y-6"
                        >
                            <div className="mb-6">
                                <div className="flex items-center gap-3 mb-2">
                                    <div className="p-2 bg-green-100 rounded-full">
                                        <EnvelopeIcon className="w-6 h-6 text-green-600"/>
                                    </div>
                                    <h2 className="text-2xl font-bold text-gray-800">이메일 주소 확인</h2>
                                </div>
                                <p className="text-sm text-gray-500 ml-11">
                                    가입을 위해 이메일 인증이 필요합니다
                                </p>
                            </div>
                            <div className="space-y-4">
                                <div className="relative">
                                    <input
                                        type="email"
                                        className={`w-full p-4 border-2 rounded-lg pl-12 text-gray-700 ${
                                            errors.email ? 'border-red-500' : 'border-gray-200'
                                        } focus:outline-none focus:border-green-500`}
                                        placeholder="example@domain.com"
                                        value={formData.email}
                                        onChange={(e) => {
                                            setFormData({...formData, email: e.target.value})
                                            setErrors(prev => ({...prev, email: ''}))
                                        }}
                                        onBlur={() => {
                                            if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
                                                setErrors(prev => ({...prev, email: '유효한 이메일 형식이 아닙니다'}))
                                            }
                                        }}
                                    />
                                    <EnvelopeIcon
                                        className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400"/>
                                </div>

                                {errors.email && (
                                    <motion.div
                                        initial={{opacity: 0, y: -10}}
                                        animate={{opacity: 1, y: 0}}
                                        className="flex items-center gap-2 text-red-500 text-sm"
                                    >
                                        <ExclamationTriangleIcon className="w-4 h-4"/>
                                        {errors.email}
                                    </motion.div>
                                )}
                            </div>

                            <button
                                onClick={handleSendVerificationCode}
                                disabled={countdown > 0 || !!errors.email}
                                className={`w-full py-3 rounded-lg font-medium flex items-center justify-center gap-2 ${
                                    countdown > 0
                                        ? 'bg-gray-100 text-gray-400'
                                        : 'bg-green-50 hover:bg-green-100 text-green-700'
                                }`}
                            >
                                {countdown > 0 ? (
                                    <>
      <span className="animate-pulse">
        {`${String(Math.floor(countdown / 60)).padStart(2, '0')}:${String(countdown % 60).padStart(2, '0')}`}
      </span>
                                        <ClockIcon className="w-4 h-4 animate-spin"/>
                                    </>
                                ) : (
                                    <>
                                        <PaperAirplaneIcon className="w-4 h-4 animate-[bounce_1.5s_infinite]"/>
                                        인증번호 받기
                                    </>
                                )}
                            </button>

                            <motion.div className="space-y-4">
                                <div className="relative">
                                    <input
                                        type="text"
                                        placeholder="8자리 인증번호 입력"
                                        className="w-full p-4 border-2 text-gray-700 ${
                                            errors.verificationCode ? 'border-red-500' : 'border-gray-200'
                                        } border-gray-200 rounded-lg pl-12 focus:outline-none focus:border-green-500"
                                        value={formData.verificationCode}
                                        onChange={(e) => setFormData({...formData, verificationCode: e.target.value})}
                                    />
                                    <LockClosedIcon
                                        className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400"/>
                                </div>

                                {errors.verificationCode && (
                                    <motion.div
                                        initial={{opacity: 0, y: -10}}
                                        animate={{opacity: 1, y: 0}}
                                        className="flex items-center gap-2 text-red-500 text-sm"
                                    >
                                        <ExclamationTriangleIcon className="w-4 h-4"/>
                                        {errors.verificationCode}
                                    </motion.div>
                                )}

                                <button
                                    onClick={handleVerifyEmail}
                                    className={`w-full py-3 rounded-lg font-medium flex items-center justify-center gap-2 ${
                                        isEmailVerified
                                            ? 'bg-green-400 cursor-not-allowed'
                                            : 'bg-green-500 hover:bg-green-600 text-white'
                                    }`}
                                >
                                    {isEmailVerified ? (
                                        <>
                                            <CheckCircleIcon className="w-5 h-5"/>
                                            인증 완료
                                        </>
                                    ) : (
                                        '인증 확인'
                                    )}
                                </button>
                            </motion.div>
                        </motion.div>
                    )}
                </AnimatePresence>

                {/* 2단계: 주소 입력 */}
                <AnimatePresence>
                    {currentStep >= 2 && (
                        <motion.div
                            initial={{opacity: 0}}
                            animate={{opacity: 1}}
                            className="space-y-4"
                        >

                            <div className="mb-6">
                                <div className="flex items-center gap-3 mb-2">
                                    <div className="p-2 bg-green-100 rounded-full">
                                        <EnvelopeIcon className="w-6 h-6 text-green-600"/>
                                    </div>
                                    <h2 className="text-2xl font-bold text-gray-800">주소 등록</h2>
                                </div>
                                <p className="text-sm text-gray-500 ml-11">
                                    가입을 위해서는 주소 인증이 필요합니다
                                </p>
                            </div>
                            <div className="flex items-center gap-2 mb-4">
                                <MapPinIcon className="w-6 h-6 text-green-500"/>
                                <h2 className="text-xl font-semibold text-gray-800">주소 입력</h2>
                            </div>

                            <AnimatePresence>
                                {(error) && (
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

                            <div className="flex gap-2">
                                <input
                                    type="text"
                                    placeholder="우편번호"
                                    className="flex-1 p-3 border-2 border-green-100 text-gray-800 rounded-lg bg-gray-50"
                                    value={formData.postalCode}
                                    readOnly
                                />
                                <motion.button
                                    type="button"
                                    whileHover={{scale: 1.05}}
                                    className="bg-green-500 text-white px-6 py-3 rounded-lg font-medium flex items-center gap-2"
                                    onClick={handleAddressSearch}
                                >
                                    <MagnifyingGlassIcon className="w-5 h-5"/>
                                    주소 검색
                                </motion.button>
                            </div>

                            {formData.baseAddress && (
                                <motion.div
                                    initial={{opacity: 0}}
                                    animate={{opacity: 1}}
                                    className="space-y-4"
                                >
                                    <input
                                        type="text"
                                        placeholder="상세 주소"
                                        className="w-full p-3 border-2 text-gray-800 border-green-100 rounded-lg"
                                        value={formData.detailAddress}
                                        onChange={(e) => setFormData({...formData, detailAddress: e.target.value})}
                                    />

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

                                    <motion.button
                                        type="button"
                                        whileHover={{ scale: 1.05 }}
                                        whileTap={{ scale: 0.95 }}
                                        className={`w-full py-3 rounded-lg font-medium flex items-center justify-center gap-2 ${
                                            formData.postalCode && formData.detailAddress
                                                ? 'bg-green-500 hover:bg-green-600 text-white'
                                                : 'bg-gray-300 cursor-not-allowed'
                                        }`}
                                        onClick={() => setCurrentStep(3)}
                                        disabled={!formData.postalCode || !formData.detailAddress}
                                    >
                                        <ArrowRightIcon className="w-5 h-5" />
                                        다음 단계로 이동
                                    </motion.button>
                                </motion.div>
                            )}
                        </motion.div>
                    )}
                </AnimatePresence>

                {/* 3단계: 추가 정보 입력 */}
                <AnimatePresence>
                    {currentStep >= 3 && (
                        <motion.div
                            initial={{opacity: 0}}
                            animate={{opacity: 1}}
                            className="space-y-4"
                        >
                            <div className="flex items-center gap-2 mb-4">
                                <UserCircleIcon className="w-6 h-6 text-green-500"/>
                                <h2 className="text-xl font-semibold text-gray-800">추가 정보</h2>
                            </div>

                            <div className="grid grid-cols-2 gap-4 text-gray-700">
                                {[
                                    {id: 'username',
                                        label: '사용자 ID',
                                        icon: <IdentificationIcon className="w-5 h-5"/>,
                                        placeholder: '사용자 id는 8~20자로 입력해 주세요'
                                    },
                                    {
                                        id: 'password',
                                        label: '비밀번호',
                                        type: 'password',
                                        icon: <LockClosedIcon className="w-5 h-5"/>,
                                        placeholder: '비밀번호는 영문+숫자 조합 8자 이상'
                                    },
                                    {
                                        id: 'checkPassword',
                                        label: '비밀번호 확인',
                                        type: 'password',
                                        icon: <LockClosedIcon className="w-5 h-5"/>
                                    },
                                    {id: 'nickname', label: '닉네임', icon: <SparklesIcon className="w-5 h-5"/>},
                                    {
                                        id: 'phoneNumber',
                                        label: '휴대폰 번호',
                                        icon: <DevicePhoneMobileIcon className="w-5 h-5"/>
                                    }
                                ].map((field) => (
                                    <motion.div
                                        key={field.id}
                                        initial={{opacity: 0}}
                                        animate={{opacity: 1}}
                                    >
                                        <div className="flex items-center gap-2 mb-2 text-gray-600">
                                            {field.icon}
                                            <label>{field.label}</label>
                                        </div>
                                        <input
                                            type={field.type || 'text'}
                                            className={`w-full p-3 border-2 rounded-lg placeholder:text-gray-400 placeholder:text-sm ${
                                                errors[field.id] ? 'border-red-500' : 'border-green-100'
                                            }`}
                                            placeholder={field.placeholder || ''}
                                            onChange={(e) => {
                                                setFormData({...formData, [field.id]: e.target.value});
                                                if(field.id === 'username') {
                                                    const isValid = e.target.value.length >=8 && e.target.value.length <=20;
                                                    setErrors(prev => ({
                                                        ...prev,
                                                        username: isValid ? '' : '사용자 ID는 8~20자로 입력해 주세요'
                                                    }));
                                                }
                                            }}
                                        />
                                        {errors.username && field.id === 'username' && (
                                            <motion.div
                                                initial={{ opacity: 0 }}
                                                animate={{ opacity: 1 }}
                                                className="text-red-500 text-sm mt-1"
                                            >
                                                {errors.username}
                                            </motion.div>
                                        )}
                                    </motion.div>
                                ))}
                            </div>
                            <button
                                className={`w-full py-4 text-lg font-semibold text-white rounded-lg flex justify-center items-center transition-colors duration-200 bg-green-500 hover:bg-green-600`}
                                type="submit"
                                onClick={handleSubmit}
                            >
                                <div className="flex items-center gap-2">
                                    <CheckCircleIcon className="h-5 w-5" />
                                    <span>가입 완료하기</span>
                                </div>
                            </button>
                        </motion.div>
                    )}
                </AnimatePresence>
            </motion.form>
        </div>
    );
}