"use client";

import {useState, useEffect} from "react";
import {useRouter} from "next/navigation"; // useRouter 훅 import
import {fetchWithAuth} from "@/app/lib/util/fetchWithAuth";
import {ExclamationCircleIcon} from "@heroicons/react/16/solid";
import {MagnifyingGlassIcon} from "@heroicons/react/24/outline";
import {CheckIcon} from "lucide-react";

export default function ClientPage() {
    const [nickname, setNickname] = useState("");
    const [phoneNumber, setPhoneNumber] = useState("");
    const [mainAddress, setMainAddress] = useState("");
    const [detailAddress, setDetailAddress] = useState("");
    const [zipcode, setZipcode] = useState("");
    const [latitude, setLatitude] = useState("");
    const [longitude, setLongitude] = useState("");

    const [isSubmitting, setIsSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState<string | null>(null);
    const [submitSuccess, setSubmitSuccess] = useState<boolean>(false);
    const [validationErrors, setValidationErrors] = useState<{
        nickname?: string;
        phoneNumber?: string;
        mainAddress?: string;
        detailAddress?: string;
        zipcode?: string;
        latitude?: string;
        longitude?: string;
    }>({});
    const [, setGeoError] = useState("");

    const BASE_URL = 'http://localhost:8080';

    const router = useRouter();

    // TODO: 쿠키 확인 및 리다이렉트 - 작동 안 함. 수정 필요
    // useEffect(() => {
    //   const authToken = document.cookie.includes('token');
    //   if (!authToken) {
    //     router.push("/");
    //   }
    // }, [router]);

    useEffect(() => {
        getData();
    }, []);

    // 위치 정보 조회
    useEffect(() => {
        if (!navigator.geolocation) {
            setGeoError("브라우저가 위치 서비스를 지원하지 않습니다");
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (position) => {
                setLatitude(position.coords.latitude.toString());
                setLongitude(position.coords.longitude.toString());
            },
            (err) => {
                setGeoError(`위치 정보 오류: ${err.message}`);
                console.error("useEffect: 위치 정보 조회 오류", err);
            }
        );
    }, []);

    // 카카오 주소 검색
    const handleAddressSearch = () => {
        if (!window.daum) {
            console.error("카카오 API 로드 실패");
            return;
        }


    new window.daum.Postcode({
      oncomplete: (data) => {
        setZipcode(data.zonecode);
        setMainAddress(`${data.address} ${data.buildingName || ""}`.trim());
      },
    // @ts-expect-error: type problem in mypage-edit in kakao address search
    }).open();
  };

    //유저정보 조회
    const getData = async () => {
        const getMyInfo = await fetchWithAuth(`${BASE_URL}/api/v1/mypage/me`, {
            method: "GET",
            credentials: "include",
            headers: {
                "Content-Type": "application/json",
            },
        });

        if (getMyInfo?.ok) {
            const Data = await getMyInfo.json();
            if (Data?.code !== "200-1") {
                console.error(`에러가 발생했습니다. \n${Data?.msg}`);
            }
            setNickname(Data?.data?.nickname);
            setPhoneNumber(Data?.data?.phoneNumber);
            setMainAddress(Data?.data?.address.mainAddress);
            setDetailAddress(Data?.data.address.detailAddress);
            setZipcode(Data?.data?.address.zipcode);
            setLatitude(Data?.data?.latitude);
            setLongitude(Data?.data?.longitude);
        } else {
            console.error("Error fetching data:", getMyInfo?.status);
        }
    };

    // 유효성 검사
    const validateNickname = (value: string) => {
        if (!value) {
            return "닉네임을 입력해주세요.";
        }
        if (!/^[가-힣]{4,10}$/.test(value)) {
            return "닉네임은 한글 4~10자로 입력해주세요.";
        }
        return "";
    };

    const validatePhoneNumber = (value: string) => {
        if (!value) {
            return "전화번호를 입력해주세요.";
        }
        if (!/^\d+$/.test(value)) {
            // 정규식 수정: 하이픈 제외, 숫자만 허용
            return "전화번호는 하이픈 없이 숫자만 입력해주세요.";
        }
        return "";
    };

    const validateMainAddress = (value: string) => {
        if (!value) {
            return "주소를 입력해주세요.";
        }
        return "";
    };

    const validateDetailAddress = (value: string) => {
        if (!value) {
            return "상세주소를 입력해주세요.";
        }
        return "";
    };

    const validateZipcode = (value: string) => {
        if (!value) {
            return "우편번호를 입력해주세요.";
        }
        return "";
    };
    const validateLatitude = (value: string) => {
        if (!value) {
            return "위도를 입력해주세요."; // 위도 필수 입력으로 변경
        }
        if (isNaN(Number(value))) {
            return "위도는 숫자 형식으로 입력해주세요.";
        }
        return "";
    };

    const validateLongitude = (value: string) => {
        if (!value) {
            return "경도를 입력해주세요."; // 경도 필수 입력으로 변경
        }
        if (isNaN(Number(value))) {
            return "경도는 숫자 형식으로 입력해주세요.";
        }
        return "";
    };

    // 입력값 변경 이벤트 핸들러
    const handleNicknameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        setNickname(value);
        setValidationErrors((prevErrors) => ({
            ...prevErrors,
            nickname: validateNickname(value),
        }));
    };

    const handlePhoneNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        setPhoneNumber(value);
        setValidationErrors((prevErrors) => ({
            ...prevErrors,
            phoneNumber: validatePhoneNumber(value),
        }));
    };

    const handleDetailAddressChange = (
        e: React.ChangeEvent<HTMLInputElement>
    ) => {
        const value = e.target.value;
        setDetailAddress(value);
        setValidationErrors((prevErrors) => ({
            ...prevErrors,
            detailAddress: validateDetailAddress(value),
        }));
    };

    //폼폼 제출 이벤트 핸들러
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        //폼 제출 전 전체 유효성 검사
        const newValidationErrors: any = {
            nickname: validateNickname(nickname),
            phoneNumber: validatePhoneNumber(phoneNumber),
            mainAddress: validateMainAddress(mainAddress),
            zipcode: validateZipcode(zipcode),
            latitude: validateLatitude(latitude.toString()),
            longitude: validateLongitude(longitude.toString()),
        };
        setValidationErrors(newValidationErrors);

        if (Object.values(newValidationErrors).some((error) => error)) {
            setSubmitError(Object.values(newValidationErrors).join("\n"));
            setIsSubmitting(false);
            setSubmitSuccess(false);
            return;
        }

        setIsSubmitting(true);
        setSubmitError(null);
        setSubmitSuccess(false);

        const body = JSON.stringify({
            nickname,
            phoneNumber,
            address: {
                mainAddress,
                detailAddress,
                zipcode,
            },
            latitude,
            longitude,
        });
        try {
            const response = await fetchWithAuth(`${BASE_URL}/api/v1/mypage/me`, {
                method: "PATCH",
                credentials: "include",
                headers: {
                    "Content-Type": "application/json",
                    Accept: "application/json",
                },
                body,
            });

            if (!response?.ok) {
                if (response) {
                    const errorData = await response.json();
                    throw new Error(errorData?.msg || response.status);
                } else {
                    throw new Error("Response is undefined");
                }
            }

            const Data = await response.json();
            if (Data?.code !== "200-1") {
                throw new Error(Data?.msg);
            }

            setSubmitSuccess(true);
        } catch (error: any) {
            console.error("마이페이지 수정 에러:", error);
            setSubmitError(
                error.message || "마이페이지 수정 중 오류가 발생했습니다."
            );
        } finally {
            setIsSubmitting(false);
        }
    };

    //모달 닫고 마이페이지로 이동
    const closeModalAndRedirect = () => {
        setSubmitSuccess(false);
        router.push("/mypage");
    };

    return (
        <div className="min-h-screen bg-gradient-to-b from-emerald-50 to-green-50 p-4 md:p-8">
            <div className="relative z-10 mx-auto px-4 md:px-8 py-12 max-w-4xl">

                <div className="absolute -top-32 -right-32 w-64 h-64 bg-emerald-200/20 rounded-full" />
                <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-emerald-300/15 rounded-full" />

                <div className="bg-white rounded-3xl shadow-xl border border-emerald-100 p-6 md:p-8">

                    <h1 className="text-3xl font-bold text-gray-600 mb-6 text-center">
                        내 정보 수정하기
                    </h1>

                    <div className="bg-white rounded-xl shadow-lg p-6 md:p-8 border border-emerald-100">
                    {/* 유저 정보 수정 폼 */}

                        {submitError && (
                            <div className="bg-red-50 border-l-4 border-red-400 p-4 rounded-lg">
                                <div className="flex items-center">
                                    <div className="flex-shrink-0">
                                        <svg
                                            className="h-5 w-5 text-red-400"
                                            xmlns="http://www.w3.org/2000/svg"
                                            viewBox="0 0 20 20"
                                            fill="currentColor"
                                        >
                                            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clipRule="evenodd" />
                                        </svg>
                                    </div>
                                    <div className="ml-3">
                                        <h3 className="text-sm font-medium text-red-800"> 수정이 불가능합니다 </h3>
                                        <div className="mt-2 text-sm text-red-700">
                                            <p>{submitError}</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        )}

                        <form onSubmit={handleSubmit} className="mt-4">

                            <div className="mb-6">
                                <label
                                    htmlFor="nickname"
                                    className="block text-sm font-medium text-gray-600 mb-2"
                                >
                                    닉네임
                                </label>
                                <input
                                    type="text"
                                    id="nickname"
                                    className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition-all text-gray-700 duration-200 ${
                                        validationErrors.nickname
                                            ? "border-emerald-500 bg-emerald-50"
                                            : "border-emerald-200 hover:border-emerald-300 hover:bg-emerald-50"
                                    }`}
                                    placeholder="닉네임을 입력해주세요"
                                    value={nickname}
                                    onChange={handleNicknameChange}
                                />
                                {validationErrors.nickname && (
                                    <div className="flex items-center gap-1.5 mt-2 text-emerald-600">
                                        <svg
                                            xmlns="http://www.w3.org/2000/svg"
                                            className="w-4 h-4 flex-shrink-0"
                                            viewBox="0 0 20 20"
                                            fill="currentColor"
                                        >
                                            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                                        </svg>
                                        <span className="text-sm">{validationErrors.nickname}</span>
                                    </div>
                                )}
                            </div>

                            <div className="mb-6">
                                <label
                                    htmlFor="phoneNumber"
                                    className="block text-sm font-medium text-gray-600 mb-2"
                                >
                                    전화번호
                                    <span className="text-emerald-600 ml-1">*</span>
                                </label>

                                <div className="relative">
                                    <input
                                        type="tel"
                                        id="phoneNumber"
                                        className={`w-full px-4 py-2.5 border rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition-all duration-200 text-gray-700 placeholder-gray-400 ${
                                            validationErrors.phoneNumber
                                                ? "border-emerald-500 bg-emerald-50"
                                                : "border-emerald-200 hover:border-emerald-300"
                                        }`}
                                        placeholder="예) 01012345678"
                                        value={phoneNumber}
                                        onChange={handlePhoneNumberChange}
                                    />

                                    {/* 유효성 검사 아이콘 */}
                                    {validationErrors.phoneNumber && (
                                        <div className="absolute inset-y-0 right-3 flex items-center">
                                            <svg
                                                className="w-5 h-5 text-emerald-500"
                                                xmlns="http://www.w3.org/2000/svg"
                                                viewBox="0 0 20 20"
                                                fill="currentColor"
                                            >
                                                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                                            </svg>
                                        </div>
                                    )}
                                </div>

                                {/* 오류 메시지 */}
                                {validationErrors.phoneNumber && (
                                    <div className="flex items-center gap-1.5 mt-2 text-emerald-600">
                                        <svg
                                            xmlns="http://www.w3.org/2000/svg"
                                            className="w-4 h-4 flex-shrink-0"
                                            viewBox="0 0 20 20"
                                            fill="currentColor"
                                        >
                                            <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                                        </svg>
                                        <span className="text-sm">{validationErrors.phoneNumber}</span>
                                    </div>
                                )}
                            </div>

                            {/* 주소 검색 섹션 */}
                            <div className="grid grid-cols-1 md:grid-cols-[1fr_auto] gap-4 mb-6">
                                <div>
                                    <label htmlFor="zipcode" className="block text-sm font-medium text-gray-600 mb-2">
                                        우편번호
                                    </label>
                                    <input
                                        type="text"
                                        id="zipcode"
                                        disabled
                                        className="w-full px-4 py-2.5 border border-emerald-200 rounded-lg bg-emerald-50 text-gray-600 focus:ring-2 focus:ring-emerald-500 disabled:opacity-75"
                                        placeholder="우편번호를 검색해주세요"
                                        value={zipcode}
                                    />
                                </div>
                                <button
                                    type="button"
                                    onClick={handleAddressSearch}
                                    className="bg-gradient-to-r from-emerald-600 to-green-600 text-white px-6 py-2.5 rounded-lg font-medium flex items-center justify-center gap-2 hover:from-emerald-700 hover:to-green-700 transition-all duration-300 mt-[28px]"
                                >
                                    <MagnifyingGlassIcon className="w-5 h-5" />
                                    주소 검색
                                </button>
                            </div>

                            {/* 주소 입력 필드 */}
                            <div className="space-y-4">
                                <div>
                                    <label htmlFor="mainAddress" className="block text-sm font-medium text-gray-600 mb-2">
                                        기본 주소
                                    </label>
                                    <div className="relative">
                                        <input
                                            type="text"
                                            id="mainAddress"
                                            disabled
                                            className="w-full px-4 py-2.5 border border-emerald-200 rounded-lg bg-emerald-50 text-gray-600 focus:ring-2 focus:ring-emerald-500 disabled:opacity-75"
                                            placeholder="주소를 검색해주세요"
                                            value={mainAddress}
                                        />
                                        {validationErrors.mainAddress && (
                                            <div className="absolute right-3 top-1/2 -translate-y-1/2">
                                                <ExclamationCircleIcon className="w-5 h-5 text-emerald-600" />
                                            </div>
                                        )}
                                    </div>
                                    {validationErrors.mainAddress && (
                                        <div className="flex items-center gap-1.5 mt-2 text-emerald-600">
                                            <ExclamationCircleIcon className="w-4 h-4 flex-shrink-0" />
                                            <span className="text-sm">{validationErrors.mainAddress}</span>
                                        </div>
                                    )}
                                </div>

                                <div>
                                    <label htmlFor="detailAddress" className="block text-sm font-medium text-gray-600 mb-2">
                                        상세 주소
                                    </label>
                                    <div className="relative">
                                        <input
                                            type="text"
                                            id="detailAddress"
                                            className="w-full px-4 py-2.5 border border-emerald-200 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 placeholder-gray-400 transition-all text-gray-700"
                                            placeholder="상세 주소를 입력해주세요"
                                            value={detailAddress}
                                            onChange={handleDetailAddressChange}
                                        />
                                        {validationErrors.detailAddress && (
                                            <div className="absolute right-3 top-1/2 -translate-y-1/2">
                                                <ExclamationCircleIcon className="w-5 h-5 text-emerald-600" />
                                            </div>
                                        )}
                                    </div>
                                    {validationErrors.detailAddress && (
                                        <div className="flex items-center gap-1.5 mt-2 text-emerald-600">
                                            <ExclamationCircleIcon className="w-4 h-4 flex-shrink-0" />
                                            <span className="text-sm">{validationErrors.detailAddress}</span>
                                        </div>
                                    )}
                                </div>
                            </div>

                            <div className="flex items-center justify-between gap-4 mt-8">
                                <button
                                    type="submit"
                                    disabled={isSubmitting}
                                    className={`relative w-full md:w-auto px-10 py-4 text-base font-semibold rounded-2xl transition-all duration-300 flex items-center justify-center gap-2
      ${
                                        isSubmitting
                                            ? 'bg-emerald-400 cursor-not-allowed'
                                            : 'bg-gradient-to-r from-emerald-600 to-green-600 hover:from-emerald-700 hover:to-green-700'
                                    }
      text-white shadow-lg hover:shadow-xl
      focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2
      disabled:opacity-75`}
                                >
                                    {isSubmitting && (
                                        <svg className="animate-spin h-5 w-5 mr-2" viewBox="0 0 24 24">
                                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none"/>
                                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"/>
                                        </svg>
                                    )}
                                    {isSubmitting ? "저장 중..." : "정보 수정 완료"}
                                    {!isSubmitting && <CheckIcon className="w-4 h-4 ml-1" />}
                                </button>

                                <button
                                    type="button"
                                    onClick={() => router.push("/mypage")}
                                    className="w-full md:w-auto px-10 py-4 text-base font-semibold text-gray-600 bg-white border-2 border-emerald-100 rounded-2xl hover:bg-emerald-50 transition-colors duration-300 shadow-lg hover:shadow-xl focus:outline-none focus:ring-2 focus:ring-gray-300 focus:ring-offset-2"
                                >
                                    취소하기
                                </button>
                            </div>

                        </form>
                    </div>
                </div>
            </div>

            {/* 성공 모달 */}
            {submitSuccess && (
                <div className="fixed inset-0 z-50 bg-black/30 backdrop-blur-sm flex items-center justify-center p-4">
                    <div className="bg-white rounded-2xl shadow-xl w-full max-w-md transform transition-all">
                        {/* 모달 컨텐츠 */}
                        <div className="p-6 text-center">
                            {/* 체크 아이콘 애니메이션 */}
                            <div
                                className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-emerald-100 mb-4">
                                <svg
                                    className="w-6 h-6 text-emerald-600 animate-check"
                                    fill="none"
                                    stroke="currentColor"
                                    viewBox="0 0 24 24"
                                >
                                    <path
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                        strokeWidth={2}
                                        d="M5 13l4 4L19 7"
                                    />
                                </svg>
                            </div>

                            {/* 타이틀 & 설명 */}
                            <h3 className="text-lg font-semibold text-gray-800 mb-2">
                                정보 수정 완료
                            </h3>
                            <p className="text-gray-600">
                                마이페이지 정보가 성공적으로 수정되었습니다
                            </p>
                        </div>

                        {/* 액션 버튼 */}
                        <div className="bg-gray-50 px-6 py-4 rounded-b-2xl">
                            <button
                                type="button"
                                onClick={closeModalAndRedirect}
                                className="w-full inline-flex justify-center rounded-lg bg-emerald-600 px-4 py-2.5 text-base font-medium text-white hover:bg-emerald-700 focus:outline-none focus-visible:ring-2 focus-visible:ring-emerald-500 focus-visible:ring-offset-2 transition-colors"
                            >
                                확인
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
