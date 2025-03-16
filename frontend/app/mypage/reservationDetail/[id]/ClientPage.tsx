// app/reservations/complete/page.jsx
"use client";

import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import "./Detail.css";
import { fetchWithAuth } from "@/app/lib/util/fetchWithAuth";
import {
  CalendarDaysIcon,
  CalendarIcon,
  CheckCircleIcon,
  CheckIcon,
  PencilIcon,
  StarIcon,
} from "lucide-react";
import { UserCircleIcon } from "@heroicons/react/24/outline";
import { InformationCircleIcon, XMarkIcon } from "@heroicons/react/16/solid";
import { ExclamationTriangleIcon, UserIcon } from "@heroicons/react/24/solid";
import Link from "next/link";

interface Reservation {
  id: number;
  status: string;
  postId: number;
  startTime: string;
  endTime: string;
  amount: number;
  rejectionReason: string;
  ownerId: number;
  renterId: number;
}

interface Deposit {
  id: number;
  status: string;
  amount: number;
  returnReason: string;
}

interface me {
  id: number;
  nickname: string;
  username: string;
  profileImage: string;
  email: string;
  phoneNumber: string;
  address: {
    mainAddress: string;
    detailAddress: string;
    zipcode: string;
  };
  latitude: number;
  longitude: number;
  createdAt: string;
  score: number;
  credit: number;
}

interface post {
  id: number;
  userId: number;
  title: string;
  priceType: string;
  price: number;
}

function formatDate(dateTimeString: string | number | Date) {
  const date = new Date(dateTimeString);
  const options: Intl.DateTimeFormatOptions = {
    month: "numeric",
    day: "numeric",
    weekday: "short",
    hour: "numeric",
    minute: "2-digit",
    hour12: true,
  };
  return date
    .toLocaleString("ko-KR", options)
    .replace(/(\d+)\.\s(\d+)\s\((.)\)\s(.+)/, "$2. $1 ($3) $4");
}

function RequestedStatus({
  reservation,
  deposit,
  me,
  renter,
  post,
  BASE_URL,
}: {
  reservation: Reservation;
  deposit: Deposit;
  me: me;
  renter: me;
  post: post;
  BASE_URL: string;
}) {
  const [showModal, setShowModal] = useState<boolean>(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false); // 추가: 승인/거절 확인 모달
  const [modalMessage, setModalMessage] = useState(""); // 추가: 모달 메시지
  const [actionType, setActionType] = useState<
    "approve" | "reject" | "cancel" | null
  >(null);
  const [rejectionReason, setRejectionReason] = useState("");

  const openModal = () => setShowModal(true);
  const closeModal = () => setShowModal(false);

  const handleApproval = () => {
    setActionType("approve");
    setModalMessage("정말 승인하시겠습니까?");
    setShowConfirmModal(true);
  };

  const handleRejection = () => {
    setActionType("reject");
    setModalMessage("정말 거절하시겠습니까?");
    setShowConfirmModal(true);
  };

  const handleCancel = () => {
    //여기도 수정
    setActionType("cancel");
    setModalMessage("정말 취소하시겠습니까?");
    setShowConfirmModal(true);
  };

  // 승인 or 거절 API
  const confirmAction = async () => {
    setShowConfirmModal(false); // 확인 모달 닫기
    if (actionType === "approve") {
      try {
        const response = await fetchWithAuth(
          `${BASE_URL}/api/v1/reservations/${reservation.id}/approve`,
          {
            method: "PATCH",
            credentials: "include",
          }
        );
        if (response?.ok) {
          window.location.reload();
        } else {
          const errorData = await response?.json();
          alert(`승인 실패: ${errorData.message || "서버 오류"}`);
        }
      } catch (error) {
        alert("승인 처리 중 오류 발생");
        console.error(error);
      }
    } else if (actionType === "reject") {
      // 거절 로직
      if (!rejectionReason.trim()) {
        alert("거절 사유를 입력해주세요.");
        return;
      }

      try {
        const response = await fetchWithAuth(
          `${BASE_URL}/api/v1/reservations/${
            reservation.id
          }/reject?reason=${encodeURIComponent(rejectionReason)}`,
          {
            method: "PATCH",
            credentials: "include",
          }
        );
        if (response?.ok) {
          setModalMessage("거절이 완료되었습니다.");
          setShowModal(true);
        } else {
          const errorData = await response?.json();
          alert(`거절 실패: ${errorData.message || "서버 오류"}`);
        }
      } catch (error) {
        alert("거절 처리 중 오류 발생");
        console.error(error);
      }
    } else if (actionType === "cancel") {
      // 취소 로직
      try {
        const response = await fetchWithAuth(
          `${BASE_URL}/api/v1/reservations/${reservation.id}/cancel`,
          {
            method: "PATCH",
            credentials: "include",
          }
        );
        if (response?.ok) {
          window.location.reload(); // 성공 시 새로고침
        } else {
          const errorData = await response?.json();
          alert(`취소 실패: ${errorData.message || "서버 오류"}`);
        }
      } catch (error) {
        alert("취소 처리 중 오류 발생");
        console.error(error);
      }
    }
  };

  return (
    <div className="flex flex-col items-center justify-center w-full max-w-4xl h-auto border border-emerald-200 rounded-2xl shadow-lg p-8 bg-gradient-to-br from-green-50 to-green-100">
      {/* 배경 디자인 요소 추가 */}
      <div className="absolute -top-16 -right-16 w-32 h-32 rounded-full bg-emerald-200/30" />
      <div className="absolute -bottom-20 -left-20 w-40 h-40 rounded-full bg-emerald-300/20" />

      {/* 헤더 섹션 재구성 */}
      <div className="mb-8 text-center space-y-4">
        <div className="inline-flex items-center justify-center p-4 bg-emerald-100 rounded-full shadow-inner">
          <CalendarIcon className="w-14 h-14 text-emerald-700" />
        </div>

        {me.id === reservation.ownerId && renter ? (
          <div className="space-y-2">
            <h2 className="text-3xl font-bold text-gray-800">
              <span
                className="text-emerald-700 hover:text-emerald-800 transition-colors cursor-pointer"
                onClick={openModal}
              >
                {renter.nickname}
              </span>
              <span className="block text-xl mt-2 text-gray-600">
                님의 예약 요청
              </span>
            </h2>
            <p className="text-sm text-gray-500">예약 상세 정보 확인</p>
          </div>
        ) : me.id === reservation.renterId ? (
          <div className="space-y-2">
            <h2 className="text-3xl font-bold text-gray-800">예약 진행 현황</h2>
            <div className="flex items-center justify-center space-x-2">
              <span className="relative flex h-3 w-3">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75" />
                <span className="relative inline-flex rounded-full h-3 w-3 bg-emerald-600" />
              </span>
              <p className="text-sm text-gray-600">호스트 승인 대기 중</p>
            </div>
          </div>
        ) : null}
      </div>

      {/* 예약 정보 카드 재디자인 */}
      <div className="w-full bg-white rounded-xl shadow-lg border border-emerald-50 divide-y-2 divide-emerald-100/50">
        <div className="p-6 space-y-1">
          <h3 className="text-xl font-semibold text-gray-800">{post.title}</h3>
          <div className="flex items-center text-gray-600">
            <CalendarDaysIcon className="w-5 h-5 text-emerald-600 mr-2" />
            <span>{formatDate(reservation.startTime)}</span>
            <span className="mx-2">→</span>
            <span>{formatDate(reservation.endTime)}</span>
          </div>
        </div>

        {/* 가격 정보 그리드 레이아웃 */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 p-6">
          <div className="space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-gray-600">대여료</span>
              <span className="font-medium text-gray-800">
                {(reservation.amount - deposit.amount).toLocaleString()} 원
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-600">보증금</span>
              <span className="font-medium text-gray-800">
                {deposit.amount.toLocaleString()} 원
              </span>
            </div>
          </div>
          <div className="bg-emerald-50 rounded-lg p-4">
            <div className="flex justify-between items-center">
              <span className="font-bold text-gray-800">총 합계</span>
              <span className="text-2xl font-bold text-emerald-700">
                {reservation.amount.toLocaleString()} 원
              </span>
            </div>
            <p className="text-xs text-gray-500 mt-2">
              ※ 보증금은 추후 크레딧으로 적립됩니다
            </p>
          </div>
        </div>
      </div>

      {/* 액션 버튼 그룹 재구성 */}
      {me.id === reservation.ownerId ? (
        <div className="mt-8 w-full grid grid-cols-2 gap-4 max-w-sm">
          <button
            className="flex items-center justify-center gap-2 p-4 rounded-xl bg-emerald-600 text-white font-medium
                   hover:bg-emerald-700 transition-all shadow-md hover:shadow-lg active:scale-95"
            onClick={handleApproval}
          >
            <CheckIcon className="w-5 h-5" />
            승인하기
          </button>
          <button
            className="flex items-center justify-center gap-2 p-4 rounded-xl bg-red-500/90 text-white font-medium
                   hover:bg-red-600 transition-all shadow-md hover:shadow-lg active:scale-95"
            onClick={handleRejection}
          >
            <XMarkIcon className="w-5 h-5" />
            거절하기
          </button>
        </div>
      ) : me.id === reservation.renterId ? (
        <button
          className="group relative mt-8 w-full max-w-sm p-4 rounded-xl
            bg-emerald-600 text-white font-medium
            transition-all duration-300
            shadow-lg hover:shadow-xl
            active:scale-[0.98]
            overflow-hidden"
          onClick={handleCancel}
        >
          {/* 호버 시 채워지는 애니메이션 레이어 */}
          <div
            className="absolute inset-0 w-full h-0 bg-emerald-700/90
                transition-all duration-300
                group-hover:h-full ease-out"
          />

          {/* 버튼 컨텐츠 */}
          <div className="relative flex items-center justify-center gap-2">
            <XMarkIcon
              className="w-5 h-5 text-emerald-100 transition-transform
                        group-hover:translate-x-1"
            />
            <span className="tracking-wide drop-shadow-sm">예약 취소하기</span>
          </div>
        </button>
      ) : null}

      {/* 확인 모달 */}
      {showConfirmModal && (
        <div
          className="fixed inset-0 flex justify-center items-center bg-emerald-100/80 backdrop-blur-sm z-50"
          id="modal"
        >
          <div className="flex flex-col items-center bg-white p-6 rounded-xl shadow-lg w-full max-w-md border border-emerald-50">
            {/* 메시지 */}
            <p className="text-gray-600 text-lg font-medium mb-4">
              {modalMessage}
            </p>

            {/* 거절 사유 입력란 */}
            {actionType === "reject" && (
              <div className="w-full">
                <input
                  type="text"
                  value={rejectionReason}
                  onChange={(e) => setRejectionReason(e.target.value)}
                  placeholder="거절 사유를 입력하세요"
                  className="border border-gray-300 rounded-lg p-3 w-full text-gray-600 focus:ring-2 focus:ring-emerald-500"
                />
              </div>
            )}

            {/* 버튼 영역 */}
            <div className="flex w-full justify-between mt-6 gap-4">
              <button
                className="flex-1 py-3 rounded-lg bg-emerald-600 text-white font-medium shadow-md hover:bg-emerald-700 transition-transform hover:scale-[1.02]"
                onClick={confirmAction}
              >
                예
              </button>
              <button
                className="flex-1 py-3 rounded-lg bg-gray-500 text-white font-medium shadow-md hover:bg-gray-600 transition-transform hover:scale-[1.02]"
                onClick={() => setShowConfirmModal(false)}
              >
                아니오
              </button>
            </div>
          </div>
        </div>
      )}

      {showModal && renter && (
        <div
          className="fixed inset-0 flex justify-center items-center bg-emerald-100/80 backdrop-blur-sm z-50"
          id="modal"
        >
          <div className="relative p-6 bg-white w-full max-w-md rounded-xl shadow-lg border border-emerald-50">
            {/* 모달 헤더 */}
            <h2 className="text-xl font-bold text-gray-600 mb-4 flex items-center gap-2">
              <UserCircleIcon className="w-6 h-6 text-emerald-600" />
              {renter.nickname} 정보
            </h2>

            {/* 모달 내용 */}
            <div className="space-y-3 text-gray-600">
              <p>
                이메일:
                <span
                  className="font-medium text-gray-800 cursor-pointer group relative inline-flex items-center"
                  title="이메일은 호스트에게만 공개됩니다."
                >
                  {" "}
                  {renter.email}
                  <span
                    className="absolute invisible group-hover:visible bg-black
                                        text-white text-xs rounded py-1 px-2 top-full left-0"
                  >
                    {" "}
                    이메일은 호스트에게만 공개됩니다.
                  </span>
                </span>
              </p>
              <p>
                <b className="text-gray-800">{renter.nickname}</b> 님의 점수는{" "}
                <span className="text-emerald-600 font-bold">
                  {renter.score}점
                </span>
                입니다!
              </p>
            </div>

            {/* 닫기 버튼 */}
            <button
              className="mt-6 w-full py-3 rounded-lg bg-emerald-100 hover:bg-emerald-200 text-emerald-700 font-medium shadow-md transition-all hover:scale-[1.02] focus:ring-2 focus:ring-emerald-500"
              onClick={closeModal}
            >
              닫기
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

function ApprovedStatus({
  reservation,
  deposit,
  me,
  owner,
  post,
  BASE_URL,
}: {
  reservation: Reservation;
  deposit: Deposit;
  me: me;
  owner: me;
  post: post;
  BASE_URL: string;
}) {
  const [showModal, setShowModal] = useState<boolean>(false);

  if (!owner) {
    return <div>Loading...</div>; // 또는 return null;
  }

  const openModal = () => setShowModal(true);
  const closeModal = () => setShowModal(false);

  const startRental = async () => {
    try {
      const response = await fetchWithAuth(
        `${BASE_URL}/api/v1/reservations/${reservation.id}/start`,
        {
          method: "PATCH",
          credentials: "include",
        }
      );
      if (response?.ok) {
        window.location.reload();
      } else {
        const errorData = await response?.json();
        alert(`승인 실패: ${errorData.message || "서버 오류"}`);
      }
    } catch (error) {
      alert("승인 처리 중 오류 발생");
      console.error(error);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center w-full max-w-4xl h-auto rounded-3xl shadow-2xl p-8 bg-gradient-to-br from-emerald-50 to-green-100 relative overflow-hidden">
      {/* 배경 디자인 요소 */}
      <div className="absolute -top-24 -right-24 w-48 h-48 bg-emerald-200/20 rounded-full" />
      <div className="absolute -bottom-32 -left-32 w-64 h-64 bg-emerald-300/15 rounded-full" />

      {/* 상태 표시 헤더 */}
      <div className="text-center space-y-6 mb-8 relative z-10">
        <div className="animate-bounce-slow">
          <div className="inline-flex p-5 bg-emerald-100 rounded-full shadow-inner">
            <span className="text-6xl">✅</span>
          </div>
        </div>
        <h1 className="text-5xl font-bold text-emerald-800 bg-clip-text bg-gradient-to-r from-emerald-600 to-green-600">
          예약 확정!
        </h1>
        <p className="text-xl text-gray-600">즐거운 이용 되세요! 🎉</p>
      </div>

      {/* 예약 정보 카드 */}
      <div className="w-full bg-white/95 backdrop-blur-sm rounded-xl shadow-lg border border-emerald-50 divide-y divide-emerald-100/50 z-10">
        {/* 상품 정보 */}
        <div className="p-6 space-y-4">
          <h2 className="text-2xl font-bold text-gray-800">{post.title}</h2>
          <div className="flex items-center text-gray-600">
            <CalendarDaysIcon className="w-6 h-6 text-emerald-600 mr-2" />
            <span className="text-lg">
              {formatDate(reservation.startTime)} ~{" "}
              {formatDate(reservation.endTime)}
            </span>
          </div>
        </div>

        {/* 가격 정보 */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 p-6">
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <span className="text-gray-600">대여료</span>
              <span className="font-medium text-gray-800">
                {(reservation.amount - deposit.amount).toLocaleString()}₩
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-600">보증금</span>
              <span className="font-medium text-gray-800">
                {deposit.amount.toLocaleString()}₩
              </span>
            </div>
          </div>
          <div className="bg-emerald-50/50 rounded-lg p-4 border border-emerald-100">
            <div className="flex justify-between items-center">
              <span className="font-bold text-gray-800">총 합계</span>
              <span className="text-2xl font-bold text-emerald-700">
                {reservation.amount.toLocaleString()}₩
              </span>
            </div>
            <p className="text-xs text-gray-500 mt-2">
              ※ 보증금은 반환 시 환급됩니다
            </p>
          </div>
        </div>
      </div>

      {/* 소유자 정보 버튼 */}
      {me.id === reservation.renterId && (
        <button
          className="mt-8 group relative w-full max-w-md p-4 rounded-xl bg-emerald-600 text-white font-medium
                 hover:bg-emerald-700 transition-all duration-300 shadow-lg hover:shadow-xl z-10"
          onClick={openModal}
        >
          <div className="flex items-center justify-center gap-3">
            <UserIcon className="w-5 h-5 text-emerald-100 transition-transform group-hover:scale-110" />
            <span className="text-lg">{owner.nickname}님 정보 보기</span>
          </div>
        </button>
      )}
      <button
        className="mt-5 bg-yellow-500 hover:bg-yellow-700 text-white font-bold py-2 px-4 rounded"
        onClick={startRental}
      >
        대여 시작하기
      </button>

      {/* 모달 창 개선 */}
      {showModal && owner && (
        <div className="fixed inset-0 flex justify-center items-center bg-black/30 backdrop-blur-sm z-50">
          <div className="relative bg-white w-11/12 md:w-1/2 lg:w-1/3 p-8 rounded-2xl shadow-2xl border border-emerald-100">
            <h2 className="text-2xl font-bold text-emerald-800 mb-6">
              {owner.nickname}님의 정보
            </h2>

            <div className="space-y-4 text-gray-600">
              <div>
                <label className="block text-sm font-medium text-emerald-700">
                  이메일
                </label>
                <p className="mt-1">{owner.email}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-emerald-700">
                  신뢰도 점수
                </label>
                <div className="inline-flex items-center mt-1 px-3 py-1 bg-emerald-100 rounded-full">
                  <StarIcon className="w-4 h-4 text-amber-400 mr-1" />
                  <span className="font-medium">{owner.score}점</span>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-emerald-700">
                  주소
                </label>
                <p className="mt-1">
                  {owner.address.mainAddress}
                  <br />
                  {owner.address.detailAddress}
                </p>
              </div>

              <div>
                <label className="block text-sm font-medium text-emerald-700">
                  전화번호
                </label>
                <p className="mt-1">{owner.phoneNumber}</p>
              </div>
            </div>

            <button
              className="mt-6 w-full py-3 bg-emerald-100 hover:bg-emerald-200 text-emerald-800 rounded-lg
                     transition-colors duration-200 font-medium"
              onClick={closeModal}
            >
              닫기
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

function InProgressStatus({
  reservation,
  deposit,
  post,
  BASE_URL,
}: {
  reservation: Reservation;
  deposit: Deposit;
  post: post;
  BASE_URL: string;
}) {
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [selectedIssue, setSelectedIssue] = useState<{
    value: string;
    label: string;
    issueType: "owner" | "renter";
  } | null>(null); // 선택된 이슈

  // 선택 가능한 사유 목록 (issueType 추가)
  const issueOptions: {
    value: string;
    label: string;
    issueType: "renter" | "owner";
  }[] = [
    { value: "DAMAGE_REPORTED", label: "물건 훼손", issueType: "renter" },
    { value: "ITEM_LOSS", label: "물건 분실", issueType: "renter" },
    {
      value: "UNRESPONSIVE_RENTER",
      label: "대여자의 무응답",
      issueType: "owner",
    }, // owner
  ];

  const handleIssue = () => {
    setShowConfirmModal(true);
  };

  const confirmAction = async () => {
    setShowConfirmModal(false);

    if (!selectedIssue) {
      alert("이유를 선택해주세요.");
      return;
    }
    let apiUrl = "";
    if (selectedIssue.issueType === "owner") {
      apiUrl = `${BASE_URL}/api/v1/reservations/${
        reservation.id
      }/ownerIssue?reason=${encodeURIComponent(selectedIssue.value)}`;
    } else if (selectedIssue.issueType === "renter") {
      apiUrl = `${BASE_URL}/api/v1/reservations/${
        reservation.id
      }/renterIssue?reason=${encodeURIComponent(selectedIssue.value)}`;
    }

    try {
      const response = await fetchWithAuth(apiUrl, {
        method: "PATCH",
        credentials: "include",
      });

      if (response?.ok) {
        window.location.reload();
      } else {
        const errorData = await response?.json();
        alert(`문제 해결 요청 실패: ${errorData.message || "서버 오류"}`);
      }
    } catch (error) {
      alert("문제 해결 요청 중 오류 발생");
      console.error(error);
    }
  };

  const completeRental = async () => {
    try {
      const response = await fetchWithAuth(
        `${BASE_URL}/api/v1/reservations/${reservation.id}/complete`,
        {
          method: "PATCH",
          credentials: "include",
        }
      );
      if (response?.ok) {
        window.location.reload();
      } else {
        const errorData = await response?.json();
        alert(`승인 실패: ${errorData.message || "서버 오류"}`);
      }
    } catch (error) {
      alert("승인 처리 중 오류 발생");
      console.error(error);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center w-full max-w-4xl h-auto rounded-3xl shadow-xl p-8 bg-gradient-to-br from-emerald-50 to-green-100 relative overflow-hidden">
      {/* 배경 디자인 요소 */}
      <div className="absolute -top-16 -right-16 w-32 h-32 bg-emerald-200/20 rounded-full" />
      <div className="absolute -bottom-20 -left-20 w-40 h-40 bg-emerald-300/15 rounded-full" />

      {/* 헤더 섹션 */}
      <div className="text-center mb-8 space-y-6">
        <div className="inline-flex p-5 bg-emerald-100 rounded-full shadow-inner animate-pulse">
          <span className="text-6xl text-emerald-600">⏳</span>
        </div>
        <h1 className="text-4xl font-bold text-emerald-800">이용 진행 중</h1>
        <p className="text-lg text-gray-600">안전한 이용 부탁드립니다</p>
      </div>

      {/* 이용 정보 카드 */}
      <div className="w-full bg-white rounded-xl shadow-lg border border-emerald-50 divide-y divide-emerald-100/50">
        <div className="p-6 space-y-4">
          <h2 className="text-2xl font-bold text-gray-800">{post.title}</h2>
          <div className="flex items-center text-gray-600">
            <CalendarDaysIcon className="w-6 h-6 text-emerald-600 mr-2" />
            <span>
              {formatDate(reservation.startTime)} ~{" "}
              {formatDate(reservation.endTime)}
            </span>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 p-6">
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <span className="text-gray-600">대여료</span>
              <span className="font-medium text-gray-800">
                {(reservation.amount - deposit.amount).toLocaleString()}₩
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-600">보증금</span>
              <span className="font-medium text-gray-800">
                {deposit.amount.toLocaleString()}₩
              </span>
            </div>
          </div>
          <div className="bg-emerald-50/50 rounded-lg p-4 border border-emerald-100">
            <div className="flex justify-between items-center">
              <span className="font-bold text-gray-800">총 합계</span>
              <span className="text-2xl font-bold text-emerald-700">
                {reservation.amount.toLocaleString()}₩
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* 문제 신고 섹션 */}
      <div className="mt-8 text-center space-y-4">
        <p className="text-lg font-medium text-gray-600">
          이용 중 문제가 있으신가요?
        </p>
        <button
          className="flex items-center justify-center gap-2 w-full max-w-sm p-4 rounded-xl bg-red-100 text-red-600 font-medium hover:bg-red-200 transition-all shadow-md hover:shadow-lg"
          onClick={handleIssue}
        >
          <ExclamationTriangleIcon className="w-5 h-5" />
          문제 신고하기
        </button>
      </div>

      <button
        className="mt-5 bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
        onClick={completeRental}
      >
        대여 종료하기
      </button>

      {/* 문제 신고 모달 */}
      {showConfirmModal && (
        <div className="fixed inset-0 flex justify-center items-center bg-black/30 backdrop-blur-sm z-50">
          <div className="bg-white w-11/12 md:w-1/2 lg:w-1/3 p-6 rounded-2xl shadow-xl border border-emerald-100">
            <h3 className="text-xl font-bold text-emerald-800 mb-4">
              문제 유형 선택
            </h3>

            <select
              value={selectedIssue?.value || ""}
              onChange={(e) => {
                const option = issueOptions.find(
                  (opt) => opt.value === e.target.value
                );
                setSelectedIssue(option || null);
              }}
              className="w-full p-3 border border-emerald-200 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition-all"
            >
              <option value="">문제 유형을 선택해주세요</option>
              {issueOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>

            <div className="grid grid-cols-2 gap-4 mt-6">
              <button
                className="p-3 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors"
                onClick={confirmAction}
              >
                신고하기
              </button>
              <button
                className="p-3 bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 transition-colors"
                onClick={() => setShowConfirmModal(false)}
              >
                취소
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function RejectedStatus({
  reservation,
  deposit,
  me,
  renter,
}: {
  reservation: Reservation;
  deposit: Deposit;
  me: me;
  renter: me;
}) {
  const [showModal, setShowModal] = useState<boolean>(false);
  const openModal = () => setShowModal(true);
  const closeModal = () => setShowModal(false);

  const router = useRouter();

  return (
    <div className="flex flex-col items-center justify-center w-full max-w-4xl h-auto rounded-3xl shadow-xl p-8 bg-gradient-to-br from-emerald-50 to-green-100 relative overflow-hidden">
      {/* 배경 디자인 요소 */}
      <div className="absolute -top-16 -right-16 w-32 h-32 bg-emerald-200/20 rounded-full" />
      <div className="absolute -bottom-20 -left-20 w-40 h-40 bg-emerald-300/15 rounded-full" />

      {/* 상태 표시 헤더 */}
      <div className="text-center mb-8 space-y-6">
        <div className="inline-flex p-4 bg-emerald-100 rounded-full shadow-inner">
          <XMarkIcon className="w-16 h-16 text-red-500 hover:text-red-600 transition-colors" />
        </div>

        {me.id === reservation.ownerId && renter ? (
          <div className="space-y-2">
            <h2 className="text-3xl font-bold text-gray-800">
              <span
                className="text-emerald-700 hover:text-emerald-800 cursor-pointer"
                onClick={openModal}
              >
                {renter.nickname}
              </span>
              <span className="block text-xl mt-2 text-gray-600">
                님의 예약 취소 완료
              </span>
            </h2>
          </div>
        ) : me.id === reservation.renterId ? (
          <div className="space-y-2">
            <h2 className="text-3xl font-bold text-gray-800">
              예약이 취소되었습니다
            </h2>
            <p className="text-gray-500">다른 상품을 찾아보세요</p>
          </div>
        ) : null}
      </div>

      {/* 취소 정보 카드 */}
      <div className="w-full bg-white rounded-xl shadow-lg border border-emerald-50 divide-y divide-emerald-100/50">
        <div className="p-6 space-y-4">
          <div className="bg-red-50/50 rounded-lg p-4 border border-red-100">
            <p className="text-sm font-medium text-red-600">취소 사유</p>
            <p className="text-lg text-gray-800 mt-1">
              {reservation.rejectionReason}
            </p>
          </div>

          <div className="flex items-center text-gray-600">
            <CalendarDaysIcon className="w-5 h-5 text-emerald-600 mr-2" />
            <span>
              {formatDate(reservation.startTime)} →{" "}
              {formatDate(reservation.endTime)}
            </span>
          </div>
        </div>

        {/* 금액 정보 */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 p-6">
          <div className="space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-gray-600">대여료</span>
              <span className="font-medium text-gray-800">
                {(reservation.amount - deposit.amount).toLocaleString()}₩
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-600">보증금</span>
              <span className="font-medium text-gray-800">
                {deposit.amount.toLocaleString()}₩
              </span>
            </div>
          </div>
          <div className="bg-emerald-50 rounded-lg p-4">
            <div className="flex justify-between items-center">
              <span className="font-bold text-gray-800">총 합계</span>
              <span className="text-2xl font-bold text-emerald-700">
                {reservation.amount.toLocaleString()}₩
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* 액션 버튼 */}
      {me.id === reservation.renterId && (
        <button
          className="mt-8 w-full max-w-sm p-4 rounded-xl bg-emerald-600 text-white font-medium
                 hover:bg-emerald-700 transition-all shadow-md hover:shadow-lg active:scale-95"
          onClick={() => router.push("/reservation")}
        >
          새로운 예약 하기
        </button>
      )}

      {/* 모달 창 개선판 */}
      {showModal && renter && (
        <div className="fixed inset-0 flex justify-center items-center bg-black/30 backdrop-blur-sm z-50">
          <div className="relative bg-white w-11/12 md:w-1/2 p-8 rounded-2xl shadow-2xl border border-emerald-100">
            <h2 className="text-2xl font-bold text-emerald-800 mb-6">
              {renter.nickname}님 정보
            </h2>

            <div className="space-y-4 text-gray-600">
              <div>
                <label className="block text-sm font-medium text-emerald-700">
                  이메일
                </label>
                <p className="mt-1">{renter.email}</p>
              </div>

              <div>
                <label className="block text-sm font-medium text-emerald-700">
                  신뢰도 점수
                </label>
                <div className="inline-flex items-center mt-1 px-3 py-1 bg-emerald-100 rounded-full">
                  <StarIcon className="w-4 h-4 text-amber-400 mr-1" />
                  <span className="font-medium">{renter.score}점</span>
                </div>
              </div>
            </div>

            <button
              className="mt-6 w-full py-3 bg-emerald-100 hover:bg-emerald-200 text-emerald-800 rounded-lg
                     transition-colors duration-200 font-medium"
              onClick={closeModal}
            >
              닫기
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

function FailedStatus({
  reservation,
  deposit,
}: {
  reservation: Reservation;
  deposit: Deposit;
}) {
  const issueOptions = [
    { value: "DAMAGE_REPORTED", label: "물건 훼손", issueType: "renter" },
    { value: "ITEM_LOSS", label: "물건 분실", issueType: "renter" },
    {
      value: "UNRESPONSIVE_RENTER",
      label: "대여자의 무응답",
      issueType: "owner",
    }, // owner
  ];

  const reasonLabel = issueOptions.find(
    (option) => option.value === deposit.returnReason
  )?.label;

  return (
    <div className="flex flex-col items-center justify-center w-full max-w-4xl h-auto rounded-3xl shadow-xl p-8 bg-gradient-to-br from-red-50 to-orange-50 relative overflow-hidden">
      {/* 배경 디자인 요소 */}
      <div className="absolute -top-16 -right-16 w-32 h-32 bg-red-200/20 rounded-full" />
      <div className="absolute -bottom-20 -left-20 w-40 h-40 bg-orange-300/15 rounded-full" />

      {/* 헤더 섹션 */}
      <div className="text-center mb-8 space-y-6">
        <div className="inline-flex p-4 bg-red-100 rounded-full shadow-inner animate-pulse">
          <ExclamationTriangleIcon className="w-16 h-16 text-red-600" />
        </div>
        <h1 className="text-4xl font-bold text-red-800">
          {reservation.status === "FAILED_OWNER_ISSUE"
            ? "소유자 문제로 예약이 중단되었습니다"
            : reservation.status === "FAILED_RENTER_ISSUE"
            ? "대여자 문제로 예약이 중단되었습니다"
            : "예약 처리 오류"}
        </h1>
      </div>

      {/* 에러 정보 카드 */}
      <div className="w-full bg-white rounded-xl shadow-lg border border-red-50 divide-y divide-red-100/50">
        {/* 실패 사유 섹션 */}
        <div className="p-6 space-y-4">
          <div className="bg-red-50/50 rounded-lg p-4 border border-red-100">
            <p className="text-sm font-medium text-red-600">상세 사유</p>
            <p className="text-lg text-gray-800 mt-1">
              {reasonLabel || deposit.returnReason}
            </p>
          </div>
        </div>

        {/* 예약 정보 그리드 */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 p-6">
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <span className="text-gray-600">예약 기간</span>
              <span className="font-medium text-gray-800">
                {formatDate(reservation.startTime)} ~{" "}
                {formatDate(reservation.endTime)}
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-600">대여 상태</span>
              <span className="font-medium text-red-600">실패</span>
            </div>
          </div>

          <div className="bg-red-50/50 rounded-lg p-4 border border-red-100">
            <div className="flex justify-between items-center">
              <span className="font-bold text-gray-800">총 환급액</span>
              <span className="text-2xl font-bold text-red-700">
                {reservation.amount.toLocaleString()}₩
              </span>
            </div>
            <p className="text-xs text-gray-500 mt-2">
              ※ 보증금 {deposit.amount.toLocaleString()}₩ 포함
            </p>
          </div>
        </div>
      </div>

      {/* 액션 버튼 그룹 */}
      <div className="mt-8 grid grid-cols-1 md:grid-cols-2 gap-4 w-full max-w-sm">
        <Link
          href="/"
          className="p-4 rounded-xl bg-emerald-600 text-white font-medium hover:bg-emerald-700 transition-all"
        >
          홈으로 이동
        </Link>
        <button className="p-4 rounded-xl bg-red-100 text-red-600 font-medium hover:bg-red-200 transition-all">
          고객센터 문의
        </button>
      </div>
    </div>
  );
}

function DoneStatus({
  reservation,
  deposit,
  post,
}: {
  reservation: Reservation;
  deposit: Deposit;
  post: post;
}) {
  const router = useRouter(); // useRouter 훅 사용

  const goToReviewPage = () => {
    router.push(`/mypage/reservationDetail/${reservation.id}/review`);
  };

  return (
    <div className="flex flex-col items-center justify-center w-full max-w-4xl h-auto rounded-3xl shadow-xl p-8 bg-gradient-to-br from-emerald-50 to-green-100 relative overflow-hidden">
      {/* 배경 디자인 요소 */}
      <div className="absolute -top-16 -right-16 w-32 h-32 bg-emerald-200/20 rounded-full" />
      <div className="absolute -bottom-20 -left-20 w-40 h-40 bg-emerald-300/15 rounded-full" />

      {/* 상태 표시 헤더 */}
      <div className="text-center mb-8 space-y-6">
        <div className="inline-flex p-4 bg-emerald-100 rounded-full shadow-inner animate-bounce-slow">
          <CheckCircleIcon className="w-16 h-16 text-emerald-600" />
        </div>
        <h1 className="text-4xl font-bold text-emerald-800">이용 완료</h1>
        <p className="text-lg text-gray-600">안전한 이용 감사드립니다</p>
      </div>

      {/* 이용 정보 카드 */}
      <div className="w-full bg-white rounded-xl shadow-lg border border-emerald-50 divide-y divide-emerald-100/50">
        {/* 상품 정보 */}
        <div className="p-6 space-y-4">
          <h2 className="text-2xl font-bold text-gray-800">{post.title}</h2>
          <div className="flex items-center text-gray-600">
            <CalendarDaysIcon className="w-6 h-6 text-emerald-600 mr-2" />
            <span>
              {formatDate(reservation.startTime)} ~{" "}
              {formatDate(reservation.endTime)}
            </span>
          </div>
        </div>

        {/* 가격 정보 그리드 */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 p-6">
          <div className="space-y-3">
            <div className="flex justify-between items-center">
              <span className="text-gray-600">대여료</span>
              <span className="font-medium text-gray-800">
                {(reservation.amount - deposit.amount).toLocaleString()}₩
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-600">보증금</span>
              <span className="font-medium text-gray-800">
                {deposit.amount.toLocaleString()}₩
              </span>
            </div>
          </div>
          <div className="bg-emerald-50/50 rounded-lg p-4 border border-emerald-100">
            <div className="flex justify-between items-center">
              <span className="font-bold text-gray-800">총 합계</span>
              <span className="text-2xl font-bold text-emerald-700">
                {reservation.amount.toLocaleString()}₩
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* 리뷰 작성 버튼 */}
      <button
        className="mt-8 flex items-center justify-center gap-2 w-full max-w-sm p-4 rounded-xl bg-emerald-600 text-white font-medium hover:bg-emerald-700 transition-all shadow-md hover:shadow-lg active:scale-95"
        onClick={goToReviewPage}
      >
        <PencilIcon className="w-5 h-5" />
        리뷰 작성하기
      </button>

      {/* 추가 정보 섹션 */}
      <div className="mt-6 text-center space-y-2">
        <p className="text-sm text-gray-500">
          보증금 환급 상태:{" "}
          <span className="font-medium text-emerald-600">완료</span>
        </p>
        <p className="text-sm text-gray-500">
          다음 이용 시 <span className="text-emerald-600">5% 추가 할인</span>{" "}
          적용
        </p>
      </div>
    </div>
  );
}

function CancelledStatus({ reservation }: { reservation: Reservation }) {
  return (
    <div className="flex flex-col items-center justify-center w-full max-w-2xl rounded-3xl shadow-xl p-8 bg-gradient-to-br from-emerald-50 to-green-50 relative overflow-hidden">
      {/* 배경 디자인 요소 */}
      <div className="absolute -top-16 -right-16 w-32 h-32 bg-emerald-200/20 rounded-full" />
      <div className="absolute -bottom-20 -left-20 w-40 h-40 bg-emerald-300/15 rounded-full" />

      {/* 상태 표시 헤더 */}
      <div className="text-center mb-8 space-y-6">
        <div className="inline-flex p-4 bg-emerald-100 rounded-full shadow-inner">
          <XMarkIcon className="w-16 h-16 text-red-500" />
        </div>
        <h2 className="text-4xl font-bold text-gray-800">
          예약이 취소되었습니다
        </h2>
      </div>

      {/* 취소 정보 카드 */}
      <div className="w-full bg-white rounded-xl shadow-lg border border-emerald-50 divide-y divide-emerald-100/50">
        <div className="p-6 space-y-4">
          <div className="bg-red-50/50 rounded-lg p-4 border border-red-100">
            <p className="text-sm font-medium text-red-600">취소 상태</p>
            <p className="text-lg text-gray-800 mt-1">
              완전히 처리된 예약 취소
            </p>
          </div>

          <div className="mt-4 space-y-2">
            <p className="text-sm text-gray-500">예약 ID</p>
            <p className="text-lg font-medium text-gray-800">
              {reservation.id}
            </p>
          </div>
        </div>

        {/* 추가 정보 섹션 */}
        <div className="p-6">
          <div className="flex items-center space-x-2 text-gray-600">
            <InformationCircleIcon className="w-5 h-5 text-emerald-600" />
            <p className="text-sm">자세한 내용은 이메일을 확인해주세요</p>
          </div>
        </div>
      </div>

      {/* 홈으로 이동 버튼 */}
      <Link
        href="/"
        className="mt-8 w-full max-w-sm p-4 rounded-xl bg-emerald-600 text-white font-medium
               hover:bg-emerald-700 transition-all shadow-md hover:shadow-lg active:scale-95"
      >
        홈으로 돌아가기
      </Link>
    </div>
  );
}

export default function ClientPage({
  reservationId,
}: {
  reservationId: number;
}) {
  const [renter, setRenter] = useState<me | null>(null);
  const [owner, setOwner] = useState<me | null>(null);
  const [me, setMe] = useState<me>({
    id: 0,
    nickname: "",
    username: "",
    profileImage: "",
    email: "",
    phoneNumber: "",
    address: {
      mainAddress: "",
      detailAddress: "",
      zipcode: "",
    },
    latitude: 0,
    longitude: 0,
    createdAt: "",
    score: 0,
    credit: 0,
  });
  const [reservation, setReservation] = useState<Reservation>({
    id: 0,
    status: "",
    postId: 0,
    startTime: "",
    endTime: "",
    amount: 0,
    rejectionReason: "",
    ownerId: 0,
    renterId: 0,
  });
  const [deposit, setDeposit] = useState<Deposit>({
    id: 0,
    status: "",
    amount: 0,
    returnReason: "",
  });
  const [post, setPost] = useState<post>({
    id: 0,
    userId: 0,
    title: "",
    priceType: "",
    price: 0,
  });

  const BASE_URL = process.env.NEXT_PUBLIC_BASE_URL;

  //유저정보 조회
  const getMe = async () => {
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
      setMe(Data?.data);
      console.log("user : ", Data?.data);
    } else {
      console.error("Error fetching data:", getMyInfo?.status);
    }
  };

  // 예약 정보 조회
  const getReservation = async () => {
    const getReservationInfo = await fetchWithAuth(
      `${BASE_URL}/api/v1/reservations/${reservationId}`,
      {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (getReservationInfo?.ok) {
      const Data = await getReservationInfo.json();
      if (Data?.code !== "200-1") {
        console.error(`에러가 발생했습니다. \n${Data?.msg}`);
      }
      setReservation(Data?.data);
    } else {
      console.error("Error fetching data:", getReservationInfo?.status);
    }
  };

  // 보증금 정보 조회
  const getDeposit = async () => {
    const getDepositInfo = await fetchWithAuth(
      `${BASE_URL}/api/v1/deposits/rid/${reservationId}`,
      {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (getDepositInfo?.ok) {
      const Data = await getDepositInfo.json();
      if (Data?.code !== "200-1") {
        console.error(`에러가 발생했습니다. \n${Data?.msg}`);
      }
      setDeposit(Data?.data);
      console.log();
    } else {
      console.error("Error fetching data:", getDepositInfo?.status);
    }
  };

  useEffect(() => {
    getMe();
    getReservation();
    getDeposit();
  }, []);

  useEffect(() => {
    if (reservation && reservation.renterId) {
      // reservation이 null이 아니고, renterId가 있을 때만
      fetchRenterInfo(reservation.renterId);
    }
    if (reservation && reservation.ownerId) {
      // reservation이 null이 아니고, ownerId가 있을 때만
      fetchOwnerInfo(reservation.ownerId);
    }
    if (reservation && reservation.postId) {
      getPost(reservation.postId);
    }
  }, [reservation]);

  const fetchRenterInfo = async (renterId: number) => {
    try {
      const response = await fetchWithAuth(
        `${BASE_URL}/api/v1/users/${renterId}`,
        {
          method: "GET",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      if (response?.ok) {
        const data = await response.json();
        console.log("Renter Info:", data.data);
        setRenter(data.data);
      } else {
        console.error("Failed to fetch renter info");
      }
    } catch (error) {
      console.error("Error fetching renter info:", error);
    }
  };

  const fetchOwnerInfo = async (ownerId: number) => {
    try {
      const response = await fetchWithAuth(
        `${BASE_URL}/api/v1/users/${ownerId}`,
        {
          method: "GET",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );
      if (response?.ok) {
        const data = await response.json();
        console.log("Owner Info:", data);
        setOwner(data.data);
      } else {
        console.error("Failed to fetch renter info");
      }
    } catch (error) {
      console.error("Error fetching renter info:", error);
    }
  };

  const getPost = async (postid: number) => {
    const getPostInfo = await fetchWithAuth(
      `${BASE_URL}/api/v1/reservations/post/${postid}`,
      {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (getPostInfo?.ok) {
      const Data = await getPostInfo.json();
      if (Data?.code !== "200-1") {
        console.error(`에러가 발생했습니다. \n${Data?.msg}`);
      }
      setPost(Data?.data);
      console.log("data : ", Data?.data);
    } else {
      console.error("Error fetching data:", getPostInfo?.status);
    }
  };

  const router = useRouter();

  const goToMyPage = () => {
    router.push("/mypage"); // /mypage 경로로 이동
  };

  return (
    <div className="flex flex-col justify-center items-center min-h-screen">
      {reservation.status === "REQUESTED" && renter && BASE_URL && (
        <RequestedStatus
          reservation={reservation}
          deposit={deposit}
          me={me}
          renter={renter}
          post={post}
          BASE_URL={BASE_URL}
        />
      )}
      {reservation.status === "APPROVED" && owner && BASE_URL && (
        <ApprovedStatus
          reservation={reservation}
          deposit={deposit}
          me={me}
          owner={owner}
          post={post}
          BASE_URL={BASE_URL}
        />
      )}
      {reservation.status === "IN_PROGRESS" && BASE_URL && (
        <InProgressStatus
          reservation={reservation}
          deposit={deposit}
          post={post}
          BASE_URL={BASE_URL}
        />
      )}
      {reservation.status === "REJECTED" && renter && (
        <RejectedStatus
          reservation={reservation}
          deposit={deposit}
          me={me}
          renter={renter}
        />
      )}
      {reservation.status === "DONE" && (
        <DoneStatus reservation={reservation} deposit={deposit} post={post} />
      )}
      {reservation.status === "FAILED_OWNER_ISSUE" && (
        <FailedStatus reservation={reservation} deposit={deposit} />
      )}
      {reservation.status === "FAILED_RENTER_ISSUE" && (
        <FailedStatus reservation={reservation} deposit={deposit} />
      )}
      {reservation.status === "CANCELED" && (
        <CancelledStatus reservation={reservation} />
      )}

      <button
        className="mt-4 w-60 py-3 rounded-lg bg-emerald-500 text-white font-medium shadow-md hover:bg-emerald-700 transition-transform hover:scale-[1.02] focus:ring-2 focus:ring-emerald-500"
        onClick={goToMyPage} // 버튼 클릭 시 goToMyPage 함수 호출
      >
        마이페이지로 이동
      </button>
    </div>
  );
}
