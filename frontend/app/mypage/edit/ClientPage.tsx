"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation"; // useRouter 훅 import

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
    // @ts-ignore
    }).open();
  };

  //유저정보 조회
  const getData = async () => {
    const getMyInfo = await fetch("http://localhost:8080/api/v1/mypage/me", {
      method: "GET",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (getMyInfo.ok) {
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
      console.error("Error fetching data:", getMyInfo.status);
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

  const handleMainAddressChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setMainAddress(value);
    setValidationErrors((prevErrors) => ({
      ...prevErrors,
      mainAddress: validateMainAddress(value),
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
  const handleZipcodeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setZipcode(value);
    setValidationErrors((prevErrors) => ({
      ...prevErrors,
      zipcode: validateZipcode(value),
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
      const response = await fetch("http://localhost:8080/api/v1/mypage/me", {
        method: "PATCH",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body,
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData?.msg || response.status);
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
    <div className="relative min-h-screen bg-gray-100">
      <div className="container mx-auto px-4 py-4 max-w-md">
        <h1 className="text-2xl font-bold text-gray-800">마이페이지 수정</h1>
        <div className="grid grid-cols-1 gap-4 mt-4">
          {/* 유저 정보 수정 폼 */}
          <div className="bg-white shadow-md p-4">
            <h2 className="text-lg font-bold text-gray-800">내 정보 수정</h2>

            {submitError && (
              <div
                className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative"
                role="alert"
              >
                <strong className="font-bold">오류!</strong>
                <span className="block sm:inline"> {submitError}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="mt-4">
              {/* 폼 입력 필드들은 이전과 동일 */}
              <div className="mb-4">
                <label
                  htmlFor="nickname"
                  className="block text-gray-700 text-sm font-bold mb-2"
                >
                  닉네임
                </label>
                <input
                  type="text"
                  id="nickname"
                  className={`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline ${
                    validationErrors.nickname ? "border-red-500" : ""
                  }`}
                  placeholder="닉네임"
                  value={nickname}
                  onChange={handleNicknameChange}
                />
                {validationErrors.nickname && (
                  <p className="text-red-500 text-xs italic mt-1">
                    {validationErrors.nickname}
                  </p>
                )}
              </div>
              <div className="mb-4">
                <label
                  htmlFor="phoneNumber"
                  className="block text-gray-700 text-sm font-bold mb-2"
                >
                  전화번호
                </label>
                <input
                  type="tel"
                  id="phoneNumber"
                  className={`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline ${
                    validationErrors.phoneNumber ? "border-red-500" : ""
                  }`}
                  placeholder="전화번호"
                  value={phoneNumber}
                  onChange={handlePhoneNumberChange}
                />
                {validationErrors.phoneNumber && (
                  <p className="text-red-500 text-xs italic mt-1">
                    {validationErrors.phoneNumber}
                  </p>
                )}
              </div>
              <div className="flex items-end justify-end mb-4">
                <button
                  type="button"
                  className="bg-green-500 text-white px-6 py-3 rounded-lg font-medium flex items-center gap-2"
                  onClick={handleAddressSearch}
                >
                  주소 검색
                </button>
              </div>
              <div className="mb-4">
                <label
                  htmlFor="mainAddress"
                  className="block text-gray-700 text-sm font-bold mb-2"
                >
                  주소
                </label>
                <input
                  type="text"
                  id="mainAddress"
                  disabled
                  className={`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline disabled:bg-gray-200 disabled:text-gray-500 ${
                    validationErrors.mainAddress ? "border-red-500" : ""
                  }`}
                  placeholder="주소"
                  value={mainAddress}
                  onChange={handleMainAddressChange}
                />
                {validationErrors.mainAddress && (
                  <p className="text-red-500 text-xs italic mt-1">
                    {validationErrors.mainAddress}
                  </p>
                )}
              </div>
              <div className="mb-4">
                <label
                  htmlFor="detailAddress"
                  className="block text-gray-700 text-sm font-bold mb-2"
                >
                  상세 주소
                </label>
                <input
                  type="text"
                  id="detailAddress"
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  placeholder="상세 주소"
                  value={detailAddress}
                  onChange={handleDetailAddressChange}
                />
                {validationErrors.detailAddress && (
                  <p className="text-red-500 text-xs italic mt-1">
                    {validationErrors.detailAddress}
                  </p>
                )}
              </div>
              <div className="mb-4">
                <label
                  htmlFor="zipcode"
                  className="block text-gray-700 text-sm font-bold mb-2"
                >
                  우편번호
                </label>
                <input
                  type="text"
                  id="zipcode"
                  disabled
                  className={`shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline disabled:bg-gray-200 disabled:text-gray-500`}
                  placeholder="우편번호"
                  value={zipcode}
                  onChange={handleZipcodeChange}
                />
                {validationErrors.zipcode && (
                  <p className="text-red-500 text-xs italic mt-1">
                    {validationErrors.zipcode}
                  </p>
                )}
              </div>
              <div className="flex items-center justify-between">
                <button
                  className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                  type="submit"
                  disabled={isSubmitting} // 요청 중에는 버튼 비활성화
                >
                  {isSubmitting ? "수정 중..." : "수정 완료"}
                </button>
                <button
                  type="button"
                  className="bg-gray-300 hover:bg-gray-400 text-gray-800 font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                  onClick={() => {
                    // TODO: 취소 버튼 클릭 시 동작 (예: 이전 페이지로 이동)
                    alert("취소 버튼 클릭!");
                  }}
                >
                  취소
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

      {/* 성공 모달 */}
      {submitSuccess && (
        <div className="fixed z-10 inset-0 overflow-y-auto">
          <div className="flex items-end justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
            <div
              className="fixed inset-0 transition-opacity"
              aria-hidden="true"
            >
              <div className="absolute inset-0 bg-gray-500 opacity-75"></div>
            </div>
            <span
              className="hidden sm:inline-block sm:align-middle sm:h-screen"
              aria-hidden="true"
            >
              &#8203;
            </span>
            <div
              className="inline-block align-bottom bg-white rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full"
              role="dialog"
              aria-modal="true"
              aria-labelledby="modal-headline"
            >
              <div className="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                <div className="sm:flex sm:items-start">
                  <div className="mt-3 text-center sm:mt-0 sm:ml-4 sm:text-left">
                    <h3
                      className="text-lg leading-6 font-medium text-gray-900"
                      id="modal-headline"
                    >
                      수정 완료
                    </h3>
                    <div className="mt-2">
                      <p className="text-sm text-gray-500">
                        마이페이지 정보가 성공적으로 수정되었습니다.
                      </p>
                    </div>
                  </div>
                </div>
              </div>
              <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                <button
                  type="button"
                  className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-blue-500 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
                  onClick={closeModalAndRedirect} // 확인 버튼 클릭 시 모달 닫고 리다이렉트
                >
                  확인
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
