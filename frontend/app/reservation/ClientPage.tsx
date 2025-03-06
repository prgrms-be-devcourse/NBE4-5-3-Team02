"use client";

import { Calendar, momentLocalizer } from "react-big-calendar";
import moment from "moment";
import "moment/locale/ko";
import "react-big-calendar/lib/css/react-big-calendar.css";
import "./CustomCalendar.css";
import { useEffect, useState } from "react";
import DateBox from "./DataBox";
import { useRouter } from "next/navigation";

interface SlotInfo {
  start: Date;
  end: Date;
  slots: Date[];
  action: "click" | "doubleClick" | "select";
}

export default function ClientPage({
  me,
  post,
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
  post: {
    userId: any;
    id: number;
    title: string;
    content: string;
    createdAt: string;
    updatedAt: string;
    category: string;
    priceType: string;
    price: number;
    latitude: number;
    longitude: number;
    viewCount: number;
  };
}) {
  const [date, setDate] = useState<Date>(new Date());
  const [selectedDates, setSelectedDates] = useState<Date[]>([]);
  const [startTime, setStartTime] = useState<string>("00:00");
  const [endTime, setEndTime] = useState<string>("00:00");
  const [showTimeForm, setShowTimeForm] = useState<boolean>(false);
  const [dateRange, setDateRange] = useState<Date[]>([]);
  const [totalPrice, setTotalPrice] = useState<number>(0);
  const [usageDuration, setUsageDuration] = useState<string>("");

  useEffect(() => {
    calculateTotalPrice(dateRange);
  }, [startTime, endTime, dateRange]);

  const handleNavigate = (newDate: Date) => {
    setDate(newDate);
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

  const localizer = momentLocalizer(moment);

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

  // 캘린더 날짜 범위 계산
  const calculateDateRange = (dates: Date[]) => {
    if (dates.length === 2) {
      const start = moment(dates[0]).startOf("day");
      const end = moment(dates[1]).startOf("day");
      const range: Date[] = [];
      let current = moment(start);

      while (current.isSameOrBefore(end, "day")) {
        range.push(current.clone().toDate());
        current.add(1, "day");
      }
      setDateRange(range);
      calculateTotalPrice(range); // 총 가격 계산
    } else {
      setDateRange([]);
      setTotalPrice(0);
    }
  };

  // 총 가격 계산
  const calculateTotalPrice = (range: Date[]) => {
    if (range.length > 0) {
      const duration = range.length;
      const price = post.price;
      const priceType = post.priceType;

      if (priceType === "HOUR") {
        const startDateTime = moment(selectedDates[0])
          .startOf("day")
          .add(moment(startTime, "HH:mm").hours(), "hours")
          .add(moment(startTime, "HH:mm").minutes(), "minutes");
        const endDateTime = moment(selectedDates[1])
          .startOf("day")
          .add(moment(endTime, "HH:mm").hours(), "hours")
          .add(moment(endTime, "HH:mm").minutes(), "minutes");
        const diff = moment.duration(endDateTime.diff(startDateTime));
        const hours = Math.ceil(diff.asHours());
        setUsageDuration(`${hours}시간 이용`);
        setTotalPrice(price * hours);
      } else if (priceType === "DAY") {
        setTotalPrice(price * duration);
        setUsageDuration(`${duration}일 이용`);
      }
    } else {
      setTotalPrice(0);
      setUsageDuration("");
    }
  };

  // 캘린더 슬롯 선택
  const handleSelectSlot = ({ start, end }: SlotInfo) => {
    const correctedEnd = moment(end).subtract(1, "day").toDate();
    setSelectedDates([start, correctedEnd]);
    calculateDateRange([start, correctedEnd]);
    setShowTimeForm(true);
  };

  // 캘린더 선택 날짜 스타일
  const dayPropGetter = (date: Date) => {
    if (dateRange.some((rangeDate) => moment(rangeDate).isSame(date, "day"))) {
      return {
        style: {
          backgroundColor: "lightblue",
        },
      };
    }
    return {};
  };

  // 시간 변경 핸들러
  const handleStartTimeChange = (time: string) => {
    setStartTime(time);
  };

  const handleEndTimeChange = (time: string) => {
    setEndTime(time);
  };
  const router = useRouter();
  // 보증금
  const deposit = 10000;

  const handleReservation = async () => {
    try {
      if (selectedDates.length === 2) {
        const startDate = moment(selectedDates[0])
          .format("YYYY-MM-DD")
          .concat(`T${startTime}:00`);
        const endDate = moment(selectedDates[1])
          .format("YYYY-MM-DD")
          .concat(`T${endTime}:00`);

        const reservationData = {
          postId: post.id,
          renterId: me.id,
          ownerId: post.userId,
          startTime: startDate,
          endTime: endDate,
          deposit: deposit,
          rentalFee: totalPrice,
        };

        const response = await fetch(
          "http://localhost:8080/api/v1/reservations/request",
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(reservationData),
          }
        );

        console.log("예약 신청 요청:", reservationData);

        if (response.ok) {
          alert("예약 신청이 완료되었습니다.");
          router.push("/");
        } else {
          const errorData = await response.json();
          console.error("예약 신청 실패:", errorData);
          alert(
            `예약 신청에 실패했습니다. ${errorData.message || "서버 오류"}`
          );
        }
      } else {
        alert("날짜를 선택해주세요.");
      }
    } catch (error) {
      console.error("예약 신청 중 오류 발생:", error);
      alert("예약 신청 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="flex flex-col items-center justify-center">
      <div className="mt-4 flex justify-center w-full">
        <Calendar
          localizer={localizer}
          startAccessor="start"
          endAccessor="end"
          style={{ height: 800 }}
          date={date}
          onNavigate={handleNavigate}
          views={{ month: true }}
          messages={messages}
          formats={formats}
          onSelectSlot={handleSelectSlot}
          selectable
          dayPropGetter={dayPropGetter}
        />
      </div>
      <div className="flex mt-10 space-x-4 justify-center">
        {" "}
        {/* 시작일, 종료일 데이터 박스 */}
        <DateBox
          date={selectedDates[0]}
          onTimeChange={handleStartTimeChange}
          time={startTime}
        />
        <DateBox
          date={selectedDates[1]}
          onTimeChange={handleEndTimeChange}
          time={endTime}
        />
      </div>
      <div className="flex flex-col justify-center items-center mt-10 w-[50%] text-xl">
        <div className="flex flex-col mt-4 w-full">
          <span>✅ 예약 시 확인해주세요!</span>
          <b className="mt-6">간단한 당부 멘트</b>
          <span>
            당일 혹은 1일 전 예약으로 확정되지 않을 수 있으니 2~3일 전에
            예약해주세요.
          </span>
        </div>
        {/* 기간 및 합계 폼 */}
        <div className="w-full">
          <div className="mt-10">
            {moment(selectedDates[0]).format("M월 DD일")} {startTime} ~{" "}
            {moment(selectedDates[1]).format("M월 DD일")} {endTime}
          </div>
          <div className="mt-1">{usageDuration}</div>
          <div className="mt-4">
            <b>대여료</b> {totalPrice}원
          </div>{" "}
          <div className="mt-1">
            {" "}
            <b>보증금</b> {deposit}원
          </div>
          <hr />
          <div className="mt-4 font-bold">합계 {totalPrice + deposit}원</div>
        </div>
        <button
          className="mt-4 mb-4 bg-green-300 text-black p-2 rounded-md w-full font-bold"
          onClick={handleReservation}
        >
          동의하고 예약 신청하기
        </button>
      </div>
    </div>
  );
}
