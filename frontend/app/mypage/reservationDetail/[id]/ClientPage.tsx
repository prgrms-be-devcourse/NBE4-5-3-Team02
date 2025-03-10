// app/reservations/complete/page.jsx
"use client";

import { c } from "framer-motion/dist/types.d-6pKw1mTI";
import { useEffect, useState } from "react";

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
  createdAt: string;
  score: number;
  credit: number;
}

function formatDate(dateTimeString: string | number | Date) {
  const date = new Date(dateTimeString);
  const options = {
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
}: {
  reservation: Reservation;
  deposit: Deposit;
  me: me;
}) {
  const [renter, setRenter] = useState<Me | null>(null);
  const [showModal, setShowModal] = useState<boolean>(false);

  useEffect(() => {
    if (me.id === reservation.ownerId) {
      fetchRenterInfo(reservation.renterId);
    }
  }, [me.id, reservation.ownerId, reservation.renterId]);

  const fetchRenterInfo = async (renterId: number) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/users/${renterId}`
      );
      if (response.ok) {
        const data = await response.json();
        console.log("Renter Info:", data);
        setRenter(data.data);
      } else {
        console.error("Failed to fetch renter info");
      }
    } catch (error) {
      console.error("Error fetching renter info:", error);
    }
  };

  const openModal = () => setShowModal(true);
  const closeModal = () => setShowModal(false);

  const handleApproval = () => {
    // 승인 로직
    console.log("승인");
  };

  const handleRejection = () => {
    // 거절 로직
    console.log("거절");
  };

  const handleCancel = () => {
    // 예약 취소 로직
    console.log("예약 취소");
  };
  return (
    <div className="flex flex-col items-center justify-center w-[70%] h-150 border rounded-lg shadow-md p-6 bg-gray-100">
      <p className="text-5xl font-bold text-blue-600 mb-4">📖</p>
      {me.id === reservation.ownerId && renter ? (
        // 호스트인 경우 대여자 이름 표시

        <p className="text-4xl mb-2 font-bold">
          <span className="text-blue-600 cursor-pointer" onClick={openModal}>
            {renter.nickname}
          </span>
          님의 예약입니다!
        </p>
      ) : me.id === reservation.renterId ? (
        // 세입자인 경우 예약 확인 메시지 표시
        <>
          <p className="text-4xl font-bold text-green-600 mb-4">
            예약을 확인 중입니다
          </p>
          <p className="text-lg mb-2">예약이 확정되면 알림으로 알려드릴게요!</p>
        </>
      ) : null}

      <p className="text-lg mb-2 font-bold">제품명 {reservation.postId}</p>
      <p className="text-lg mb-2">
        {formatDate(reservation.startTime)} ~ {formatDate(reservation.endTime)}
      </p>
      <p className="text-lg mb-8">
        대여료 {reservation.amount - deposit.amount}₩ + 보증금 {deposit.amount}₩
      </p>
      <p className="text-lg mb-2 font-bold">합계 {reservation.amount}₩</p>
      {me.id === reservation.ownerId ? (
        // 호스트인 경우 승인/거절 버튼 표시
        <div className="flex mt-4 space-x-4">
          <button
            className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
            onClick={handleApproval}
          >
            승인
          </button>
          <button
            className="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded"
            onClick={handleRejection}
          >
            거절
          </button>
        </div>
      ) : me.id === reservation.renterId ? (
        // 세입자인 경우 예약 취소 버튼 표시
        <button
          className="mt-4 bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded"
          onClick={handleCancel}
        >
          예약 취소
        </button>
      ) : null}
      {showModal && renter && (
        <div className="fixed inset-0 flex justify-center items-center bg-transparent backdrop-filter backdrop-blur-lg">
          <div className="relative p-8 bg-white w-1/2 rounded-lg">
            <h2 className="text-2xl font-bold mb-4">{renter.nickname} 정보</h2>
            <p>이메일: {renter.email}</p>
            <p>
              <b>{renter.nickname}</b> 님의 점수는 {renter.score}점입니다!
            </p>
            <button
              className="mt-4 bg-gray-300 hover:bg-gray-400 text-gray-800 font-bold py-2 px-4 rounded"
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
}: {
  reservation: Reservation;
  deposit: Deposit;
}) {
  return (
    <div className="flex flex-col items-center justify-center w-[70%] h-150 border rounded-lg shadow-md p-6 bg-green-200">
      <p className="text-5xl font-bold text-green-600 mb-4">✅</p>
      <p className="text-4xl font-bold text-green-600 mb-4">
        예약이 확정되었습니다!
      </p>
      <p className="text-lg mb-2">즐거운 이용 되세요!</p>
      <p className="text-lg mb-2 font-bold">제품명 {reservation.postId}</p>
      <p className="text-lg mb-2">
        {formatDate(reservation.startTime)} ~ {formatDate(reservation.endTime)}
      </p>
      <p className="text-lg mb-8">
        대여료 {reservation.amount - deposit.amount}₩ + 보증금 {deposit.amount}₩
      </p>
      <p className="text-lg mb-2 font-bold">합계 {reservation.amount}₩</p>
    </div>
  );
}

function InProgressStatus({
  reservation,
  deposit,
}: {
  reservation: Reservation;
  deposit: Deposit;
}) {
  return (
    <div className="flex flex-col items-center justify-center w-[70%] h-150 border rounded-lg shadow-md p-6 bg-yellow-200">
      <p className="text-5xl font-bold text-yellow-600 mb-4">⏳</p>
      <p className="text-4xl font-bold text-yellow-600 mb-4">이용 중입니다</p>
      <p className="text-lg mb-2">이용이 완료되면 반납해주세요!</p>
      <p className="text-lg mb-2 font-bold">제품명 {reservation.postId}</p>
      <p className="text-lg mb-2">
        {formatDate(reservation.startTime)} ~ {formatDate(reservation.endTime)}
      </p>
      <p className="text-lg mb-8">
        대여료 {reservation.amount - deposit.amount}₩ + 보증금 {deposit.amount}₩
      </p>
      <p className="text-lg mb-2 font-bold">합계 {reservation.amount}₩</p>
    </div>
  );
}

function RejectedStatus({
  reservation,
  deposit,
}: {
  reservation: Reservation;
  deposit: Deposit;
}) {
  return (
    <div className="flex flex-col items-center justify-center w-[70%] h-150 border rounded-lg shadow-md p-6 bg-red-200">
      <p className="text-5xl font-bold text-red-600 mb-4">❌</p>
      <p className="text-4xl font-bold text-red-600 mb-4">
        예약이 거절되었습니다
      </p>
      <p className="text-lg mb-2">다른 예약을 시도해주세요.</p>
      <p className="text-lg mb-2 font-bold">사유</p>
      <p className="text-lg mb-2 font-bold">{reservation.rejectionReason}</p>
      <p className="text-lg mb-2">
        {formatDate(reservation.startTime)} ~ {formatDate(reservation.endTime)}
      </p>
      <p className="text-lg mb-8">
        대여료 {reservation.amount - deposit.amount}₩ + 보증금 {deposit.amount}₩
      </p>
      <p className="text-lg mb-2 font-bold">합계 {reservation.amount}₩</p>
      <button className="text-lg bg-red-500 text-white px-4 py-2 rounded-lg">
        다시 예약하기
      </button>
    </div>
  );
}

function DoneStatus({
  reservation,
  deposit,
}: {
  reservation: Reservation;
  deposit: Deposit;
}) {
  return (
    <div className="flex flex-col items-center justify-center w-[70%] h-150 border rounded-lg shadow-md p-6 bg-gray-200">
      <p className="text-5xl font-bold text-gray-600 mb-4">🏁</p>
      <p className="text-4xl font-bold text-gray-600 mb-4">
        이용이 완료되었습니다
      </p>
      <p className="text-lg mb-2">이용해주셔서 감사합니다.</p>
      <p className="text-lg mb-2 font-bold">제품명 {reservation.postId}</p>
      <p className="text-lg mb-2">
        {formatDate(reservation.startTime)} ~ {formatDate(reservation.endTime)}
      </p>
      <p className="text-lg mb-8">
        대여료 {reservation.amount - deposit.amount}₩ + 보증금 {deposit.amount}₩
      </p>
      <p className="text-lg mb-2 font-bold">합계 {reservation.amount}₩</p>
    </div>
  );
}

export default function ClientPage({
  reservation,
  deposit,
  me,
}: {
  reservation: {
    id: number;
    status: string;
    postId: number;
    startTime: string;
    endTime: string;
    amount: number;
    rejectionReason: string;
    ownerId: number;
    renterId: number;
  };
  deposit: {
    id: number;
    status: string;
    amount: number;
    returnReason: string;
  };
  me: {
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
    createdAt: string;
    score: number;
    credit: number;
  };
}) {
  return (
    <div className="flex justify-center items-center min-h-screen">
      {reservation.status === "REQUESTED" && (
        <RequestedStatus reservation={reservation} deposit={deposit} me={me} />
      )}
      {reservation.status === "APPROVED" && (
        <ApprovedStatus reservation={reservation} deposit={deposit} />
      )}
      {reservation.status === "IN_PROGRESS" && (
        <InProgressStatus reservation={reservation} deposit={deposit} />
      )}
      {reservation.status === "REJECTED" && (
        <RejectedStatus reservation={reservation} deposit={deposit} />
      )}
      {reservation.status === "DONE" && (
        <DoneStatus reservation={reservation} deposit={deposit} />
      )}
    </div>
  );
}
