// app/reservations/complete/page.jsx
"use client";

import {CalendarDaysIcon} from "lucide-react";

function formatDate(dateTimeString: string | number | Date) {
  const date = new Date(dateTimeString);
  const options: Intl.DateTimeFormatOptions = {
    // 타입 명시
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

export default function ClientPage({
  reservation,
  deposit,
  post,
}: {
  reservation: {
    id: number;
    status: string;
    postId: number;
    startTime: string;
    endTime: string;
    amount: number;
  };
  deposit: {
    id: number;
    status: string;
    amount: number;
    returnReason: string;
  };
  post: {
    id: number;
    userId: number;
    title: string;
    priceType: string;
    price: number;
  };
}) {
  const formattedStartTime = formatDate(reservation.startTime);
  const formattedEndTime = formatDate(reservation.endTime);


  return (
      <div className="flex justify-center items-center min-h-screen bg-gradient-to-br from-emerald-50 to-green-50">
        <div className="relative w-full max-w-2xl h-auto rounded-3xl shadow-xl p-8 bg-white/95 backdrop-blur-sm border border-emerald-100">
          {/* 애니메이션 배경 요소 */}
          <div className="absolute -top-16 -right-16 w-32 h-32 bg-emerald-200/20 rounded-full" />
          <div className="absolute -bottom-20 -left-20 w-40 h-40 bg-emerald-300/15 rounded-full" />

          {/* 헤더 섹션 */}
          <div className="text-center space-y-6 mb-8">
            <div className="inline-flex p-4 bg-emerald-100 rounded-full shadow-inner animate-pulse">
              <span className="text-6xl">⏳</span>
            </div>
            <h1 className="text-4xl font-bold text-emerald-800">
              예약 확인 중입니다
            </h1>
          </div>

          {/* 예약 정보 카드 */}
          <div className="space-y-6 divide-y divide-emerald-100/50">
            {/* 상품 정보 */}
            <div className="space-y-4 pb-6">
              <h2 className="text-2xl font-bold text-gray-800">{post.title}</h2>
              <div className="flex items-center text-gray-600">
                <CalendarDaysIcon className="w-6 h-6 text-emerald-600 mr-2" />
                <span className="text-lg">
            {formattedStartTime} ~ {formattedEndTime}
          </span>
              </div>
            </div>

            {/* 가격 정보 그리드 */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-6">
              <div className="space-y-4">
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

              {/* 총 합계 강조 박스 */}
              <div className="bg-emerald-50/50 rounded-xl p-4 border border-emerald-100">
                <div className="flex justify-between items-center">
                  <span className="font-bold text-gray-800">총 합계</span>
                  <span className="text-2xl font-bold text-emerald-700">
              {reservation.amount.toLocaleString()}₩
            </span>
                </div>
                <p className="text-xs text-gray-500 mt-2">※ 보증금은 반환 시 환급됩니다</p>
              </div>
            </div>
          </div>

          {/* 진행 상태 표시기 */}
          <div className="mt-8 flex flex-col items-center space-y-4">
            <div className="flex space-x-2">
              {[...Array(3)].map((_, idx) => (
                  <div
                      key={idx}
                      className="w-3 h-3 rounded-full bg-emerald-200 animate-pulse"
                      style={{ animationDelay: `${idx * 0.2}s` }}
                  />
              ))}
            </div>
            <p className="text-sm text-gray-500">호스트의 최종 확인을 기다리고 있습니다</p>
          </div>
        </div>
      </div>
  );
}
