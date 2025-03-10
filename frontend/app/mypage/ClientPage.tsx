"use client";

import { Calendar, momentLocalizer } from "react-big-calendar";
import moment from "moment";
import "moment/locale/ko";
import "react-big-calendar/lib/css/react-big-calendar.css";
import { useMemo, useState } from "react";
import ScoreIcon from "../lib/util/scoreIcon";
import Link from "next/link";

export default function ClientPage({
  me,
  reservations,
}: {
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
  reservations: {
    rentals: Array<{
      id: number;
      title: string;
      image: string;
      amount: number;
      startTime: string;
      endTime: string;
      status: string;
      isReviewed: boolean;
    }>;
    borrows: Array<{
      id: number;
      title: string;
      image: string;
      amount: number;
      startTime: string;
      endTime: string;
      status: string;
      isReviewed: boolean;
    }>;
  };
}) {
  const localizer = momentLocalizer(moment);

  const [eventType, setEventType] = useState("rental");
  const [date, setDate] = useState(new Date());

  const handleNavigate = (newDate: Date) => {
    setDate(newDate);
  };

  const getRandomColor = () => {
    const colors = [
      "#5A67D8",
      "#48BB78",
      "#ED8936",
      "#F56565",
      "#4299E1",
      "#667EEA",
      "#9AE6B4",
      "#DD6B20",
      "#E53E3E",
      "#3182CE",
      "#805AD5",
      "#68D391",
      "#C05621",
      "#C53030",
      "#2B6CB0",
      "#A371F7",
      "#48B088",
      "#D69E2E",
      "#D53F8C",
      "#2A4365",
    ];
    return colors[Math.floor(Math.random() * colors.length)];
  };
  const scheduleReservations = {
    rentals: reservations.rentals.filter(
      (rental) =>
        rental.status === "APPROVED" || rental.status === "IN_PROGRESS"
    ),
    borrows: reservations.borrows.filter(
      (borrow) =>
        borrow.status === "APPROVED" || borrow.status === "IN_PROGRESS"
    ),
  };
  const rentalEvents = scheduleReservations.rentals.map((rental) => ({
    id: rental.id,
    title: `빌리기: ${rental.title}`,
    start: moment(rental.startTime).toDate(),
    end: moment(rental.endTime).toDate(),
    color: getRandomColor(),
  }));
  const borrowEvents = scheduleReservations.borrows.map((borrow) => ({
    id: borrow.id,
    title: `빌려주기: ${borrow.title}`,
    start: new Date(borrow.startTime),
    end: new Date(borrow.endTime),
    color: getRandomColor(),
  }));

  const messages = {
    today: "오늘",
    previous: "이전",
    next: "다음",
    month: "월",
    week: "주",
    day: "일",
    agenda: "일정",
    date: "날짜",
    time: "시간",
    event: "이벤트",
  };

  const reservationStatus: {
    [key: string]: string;
  } = {
    REQUESTED: "요청됨",
    APPROVED: "승인됨",
    IN_PROGRESS: "대여중",
    REJECTED: "거절됨",
    DONE: "완료됨",
    FAILED_OWNER_ISSUE: "대여자 이슈로 실패",
    FAILED_RENTER_ISSUE: "대여자 이슈로 실패",
  };

  const formats = {
    dateFormat: "D",
    dayFormat: (date: Date, culture: any, localizer: any) =>
      localizer.format(date, "dddd", culture),
    weekdayFormat: (date: Date, culture: any, localizer: any) =>
      localizer.format(date, "ddd", culture),
    monthHeaderFormat: (date: Date, culture: any, localizer: any) =>
      localizer.format(date, "YYYY년 MM월", culture),
  };

  const filteredReservations = useMemo(() => {
    if (eventType === "rental") {
      return reservations.rentals;
    } else {
      return reservations.borrows;
    }
  }, [eventType, reservations]);

  return (
    <div className="relative min-h-screen bg-gray-100">
      <div className="container mx-auto px-4 py-4">
        <div className="flex justify-between items-center">
          <h1 className="text-2xl font-bold text-gray-800">마이페이지</h1>
          <Link href="/mypage/edit">
            <button className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded">
              마이페이지 수정
            </button>
          </Link>
        </div>
        <div className="grid grid-cols-1 gap-4 mt-4">
          {/* 유저 정보 */}
          <div className="bg-white shadow-md p-4">
            <h2 className="text-lg font-bold text-gray-800">나의 정보</h2>
            <div className="mt-4">
              <p className="text-gray-800">
                <span className="font-bold">닉네임: </span>
                {me.nickname}
              </p>
              <p className="text-gray-800">
                <span className="font-bold">전화번호: </span>
                {me.phoneNumber}
              </p>
              {me?.username ? (
                <p className="text-gray-800">
                  <span className="font-bold">아이디: </span> {me.username}
                </p>
              ) : (
                <p className="text-gray-800">
                  <span className="font-bold">이메일: </span> {me.email}
                </p>
              )}
              <p className="text-gray-800">
                <span className="font-bold">주소: </span>{" "}
                {me.address.mainAddress} {me.address.detailAddress} (
                {me.address.zipcode})
              </p>
              <p className="text-gray-800">
                <span className="font-bold">가입일:</span>{" "}
                {localizer.format(me.createdAt, "YYYY년 MM월 DD일일", "ko")}
              </p>
              <p className="text-gray-800">
                <span className="flex flex-row">
                  <span className="font-bold">평점: </span>
                  {me.score}
                  <ScoreIcon
                    className="ml-2"
                    score={me.score}
                    size={25}
                    round
                  />
                </span>
              </p>
              <p className="text-gray-800">
                <span className="font-bold">크레딧: </span> {me.credit}
              </p>
            </div>
          </div>

          {/* 예약 현황 */}
          <div className="bg-white shadow-md p-4">
            <h2 className="text-lg font-bold text-gray-800">예약 현황</h2>
            <div className="flex border-b-2 mh-4">
              <button
                onClick={() => setEventType("rental")}
                className={`flex-1 py-2 text-center text-lg font-semibold border-1 cursor-pointer transition-all
                ${
                  eventType === "rental"
                    ? "bg-white text-gray-800 border-b-2 border-gray-800"
                    : "bg-gray-200 text-gray-800 hover:bg-white active:bg-white"
                }`}
              >
                빌리기
              </button>
              <button
                onClick={() => setEventType("borrow")}
                className={`flex-1 py-2 text-center text-lg font-semibold border-1 cursor-pointer transition-all
                ${
                  eventType === "borrow"
                    ? "bg-white text-gray-800 border-b-2 border-gray-800"
                    : "bg-gray-200 text-gray-800 hover:bg-white active:bg-white"
                }`}
              >
                빌려주기
              </button>
            </div>
            <div className="grid grid-cols-1 gap-4 mt-4">
              {/* 캘린더 */}
              <div className="mt-4">
                <Calendar
                  localizer={localizer}
                  events={eventType === "rental" ? rentalEvents : borrowEvents}
                  startAccessor="start"
                  endAccessor="end"
                  style={{ height: 800 }}
                  date={date}
                  onNavigate={handleNavigate}
                  eventPropGetter={(event) => ({
                    style: { backgroundColor: event.color, color: "white" },
                  })}
                  views={{ month: true }}
                  messages={messages}
                  formats={formats}
                />
              </div>
              {/* 예약 목록 */}
              <div>
                {filteredReservations.length > 0 ? (
                  filteredReservations.map((reservation) => (
                    <div
                      key={reservation.id}
                      className="flex items-center border rounded p-2 mb-2"
                    >
                      <img
                        src={reservation.image}
                        alt={reservation.title}
                        className="w-20 h-20 object-cover rounded mr-4"
                      />
                      <div>
                        <h3 className="font-bold text-gray-800">
                          {reservation.title}
                        </h3>
                        <p className="text-gray-600">
                          대여:{" "}
                          {moment(reservation.startTime).format(
                            "YYYY년MM월DD일 HH시mm분"
                          )}
                        </p>
                        <p className="text-gray-600">
                          반납:{" "}
                          {moment(reservation.endTime).format(
                            "YYYY년MM월DD일 HH시mm분"
                          )}
                        </p>
                        <p className="text-gray-600"></p>
                        <p className="text-gray-600">
                          가격: {reservation.amount}원
                        </p>
                        <p className="text-gray-600">
                          상태: {reservationStatus[reservation.status] || ""}
                        </p>
                      </div>
                    </div>
                  ))
                ) : (
                  <p>예약 내역이 없습니다.</p>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
