"use client";

import { Calendar, momentLocalizer } from "react-big-calendar";
import moment from "moment";
import "moment/locale/ko";
import "react-big-calendar/lib/css/react-big-calendar.css";
import "./CustomCalendar.css";
import { useEffect, useState } from "react";
import DateBox from "./DataBox";
import { useRouter } from "next/navigation";
import { fetchWithAuth } from "@/app/lib/util/fetchWithAuth";

interface SlotInfo {
  start: Date;
  end: Date;
  slots: Date[];
  action: "click" | "doubleClick" | "select";
}

interface Availability {
  date: string | null;
  startTime: string;
  endTime: string;
  recurrenceDays: number;
  recurring: boolean;
}

interface PostDetail {
  id: number;
  userid: string;
  nickname: string;
  title: string;
  content: string;
  category: string;
  priceType: string;
  price: number;
  latitude: number;
  longitude: number;
  createdAt: string | null;
  viewCount: number;
  images: string[];
  availabilities: Availability[];
}

interface post {
  id: number;
  userId: number;
  title: string;
  priceType: string;
  price: number;
}

export default function ClientPage({ postid }: { postid: number }) {
  const [date, setDate] = useState<Date>(new Date());
  const [selectedDates, setSelectedDates] = useState<Date[]>([]);
  const [startTime, setStartTime] = useState<string>("00:00");
  const [endTime, setEndTime] = useState<string>("00:00");
  const [dateRange, setDateRange] = useState<Date[]>([]);
  const [totalPrice, setTotalPrice] = useState<number>(0);
  const [usageDuration, setUsageDuration] = useState<string>("");
  const [reservedDates, setReservedDates] = useState<Date[]>([]);
  const [events, setEvents] = useState<any[]>([]);
  const [postDetail, setPostDetail] = useState<PostDetail | null>(null);

  const [post, setPost] = useState<post>({
    id: 0,
    userId: 0,
    title: "",
    priceType: "",
    price: 0,
  });

  const BASE_URL = process.env.NEXT_PUBLIC_BASE_URL;

  const dynamicUri = window.location.href; // 현재 브라우저의 URL 가져오기
  function extractIdFromUri(uri: any) {
    // 정규 표현식으로 posts 뒤에 오는 숫자를 캡처
    const match = uri.match(/\/posts\/(\d+)\//);
    // 캡처된 ID 반환 (없으면 null)

    return match ? match[1] : null;
  }

  const id = extractIdFromUri(dynamicUri);
  const numericId = Number(id);

  useEffect(() => {
    const fetchPostDetail = async () => {
      try {
        const response = await fetchWithAuth(
          `${BASE_URL}/api/v1/posts/${numericId}`,
          {
            method: "GET",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
          }
        );

        if (!response?.ok)
          throw new Error("게시물을 불러오는 데 실패했습니다.");

        const data = await response.json();
        console.log("넘어온 데이터 확인: ", data.data);
        setPostDetail(data.data);
      } catch (err) {
        console.log(err);
      }
    };

    fetchPostDetail();
  }, [numericId]);

  // availabilities를 const로 추출
  const availabilities = postDetail?.availabilities || [];
  console.log(availabilities);

  const extractedTimes = availabilities.map((item) => {
    const [startDate, startTime] = item.startTime.split(" ");
    const [endDate, endTime] = item.endTime.split(" ");

    return {
      startDate, // 시작 날짜
      startTime, // 시작 시간
      endDate, // 종료 날짜
      endTime, // 종료 시간
    };
  });
  console.log("이용 가능 날짜 시간", extractedTimes);

  const isClickableDate = (date: Date) => {
    const result = extractedTimes.some((time) => {
      const startDate = moment(time.startDate).startOf("day"); // 시작 날짜
      const endDate = moment(time.endDate).startOf("day"); // 종료 날짜

      // 로그 출력: 현재 확인 중인 날짜와 클릭 가능한 날짜 범위
      console.log("현재 확인 중인 날짜:", moment(date).format("YYYY-MM-DD"));
      console.log("클릭 가능한 시작 날짜:", startDate.format("YYYY-MM-DD"));
      console.log("클릭 가능한 종료 날짜:", endDate.format("YYYY-MM-DD"));

      const isSameStart = moment(date).isSame(startDate, "day");
      const isSameEnd = moment(date).isSame(endDate, "day");

      // 로그 출력: 클릭 가능 여부
      console.log("현재 날짜가 시작 날짜와 동일한가?", isSameStart);
      console.log("현재 날짜가 종료 날짜와 동일한가?", isSameEnd);

      return isSameStart || isSameEnd;
    });
    // 최종 결과 로그 출력
    return result;
  };

  const handleNavigate = (newDate: Date) => {
    console.log("사용자가 클릭한 날짜:", newDate.toLocaleDateString());
    // 클릭 가능한지 확인
    const clickable = isClickableDate(newDate);

    if (clickable) {
      console.log("해당 날짜는 클릭 가능합니다.");
      setDate(newDate); // 상태 업데이트
    }
    setDate(newDate); // 상태 업데이트
  };

  const getPost = async () => {
    const getMyInfo = await fetchWithAuth(
      `${BASE_URL}/api/v1/reservations/post/${postid}`,
      {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (getMyInfo?.ok) {
      const Data = await getMyInfo.json();
      if (Data?.code !== "200-1") {
        console.error(`에러가 발생했습니다. \n${Data?.msg}`);
      }
      setPost(Data?.data);
      console.log("data : ", Data?.data);
    } else {
      console.error("Error fetching data:", getMyInfo?.status);
    }
  };

  useEffect(() => {
    calculateTotalPrice(dateRange);
  }, [startTime, endTime, dateRange]);

  useEffect(() => {
    getPost();
    const loadReservedEvents = async () => {
      const events = await fetchReservedEvents(post.id);
      setEvents(events);
      console.log("Reserved events:", events);
      processReservedEvents(events);
    };
    loadReservedEvents();
  }, []);

  // 예약된 날짜 가져오기
  const fetchReservedEvents = async (postId: number) => {
    try {
      const response = await fetchWithAuth(
        `${BASE_URL}/api/v1/reservations/reservatedDates/${postId}`,
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
        return data.data.map((reservation: any) => ({
          title: `예약중`,
          start: moment(reservation.startTime).toDate(),
          end: moment(reservation.endTime).toDate(),
          allDay: false,
        }));
      } else {
        console.error("Failed to fetch reserved events");
        return [];
      }
    } catch (error) {
      console.error("Error fetching reserved events:", error);
      return [];
    }
  };

  const processReservedEvents = (events: any[]) => {
    const dates: Date[] = [];
    events.forEach((event) => {
      const currentDate = moment(event.start).clone();
      const endDate = moment(event.end).clone();

      while (currentDate.isSameOrBefore(endDate, "day")) {
        dates.push(currentDate.clone().toDate());
        currentDate.add(1, "day");
      }
    });
    setReservedDates(dates);
  };

  const formats = {
    dateFormat: "D",
    dayFormat: (date: Date, culture: any, localizer: any) =>
      localizer.format(date, "D", culture), // 숫자만 표시
    weekdayFormat: (date: Date, culture: any, localizer: any) =>
      localizer.format(date, "ddd", culture), // 약식 요일(3자)
    monthHeaderFormat: (date: Date, culture: any, localizer: any) =>
      localizer.format(date, "YYYY년 M월", culture), // "2024년 3월" 형식
    eventTimeRangeFormat: () => "", // 시간 표시 제거
    dayHeaderFormat: (date: Date, culture: any, localizer: any) =>
      localizer.format(date, "MMM D", culture), // "3월 14" 형식
  };

  const localizer = momentLocalizer(moment);

  const messages = {
    today: "오늘",
    previous: "◀",
    next: "▶",
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
      const current = moment(start);

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

  // 캘린더 슬롯 선택 - 이미 예약된 날짜는 선택 불가능
  const handleSelectSlot = ({ start, end }: SlotInfo) => {
    const correctedEnd = moment(end).subtract(1, "day").toDate(); // 종료 날짜 수정
    const range: Date[] = [];
    const current = moment(start).clone();

    // 클릭 가능한 날짜 확인 함수
    const isClickableDate = (date: Date) => {
      return availabilities.some((availability) => {
        const startDate = moment(availability.startTime).startOf("day");
        const endDate = moment(availability.endTime).startOf("day");
        return (
          moment(date).isSame(startDate, "day") ||
          moment(date).isSame(endDate, "day")
        );
      });
    };

    while (current.isSameOrBefore(moment(correctedEnd), "day")) {
      // 이미 예약된 날짜 확인
      const isReserved = reservedDates.some((reservedDate) =>
        current.isSame(moment(reservedDate), "day")
      );

      // 클릭 가능한 날짜 확인
      const clickable = isClickableDate(current.toDate());

      if (isReserved) {
        alert("선택하신 날짜는 이미 예약되어 있습니다.");
        return;
      }

      if (!clickable) {
        alert("선택하신 날짜는 이용할 수 없습니다.");
        return;
      }

      range.push(current.clone().toDate());
      current.add(1, "day");
    }

    setSelectedDates([start, correctedEnd]);
    calculateDateRange([start, correctedEnd]);
  };

  const dayPropGetter = (date: Date) => {
    const isReserved = reservedDates.some((reservedDate) =>
      moment(date).isSame(moment(reservedDate), "day")
    );

    const isClickable = availabilities.some((availability) => {
      const startDate = moment(availability.startTime).startOf("day");
      const endDate = moment(availability.endTime).startOf("day");
      return (
        moment(date).isSame(startDate, "day") ||
        moment(date).isSame(endDate, "day")
      );
    });

    // 공통 클래스
    const baseClasses = `
    transition-colors 
    duration-200 
    !text-gray-600
  `;

    if (isReserved) {
      return {
        className: `
      ${baseClasses}
      bg-red-100/80
      text-red-800
      hover:bg-red-200/60
      cursor-not-allowed
      pointer-events-none
    `,
      };
    }

    if (!isClickable) {
      return {
        className: `
        ${baseClasses}
        bg-gray-100/80
        text-gray-500
        hover:bg-gray-200/60
        cursor-not-allowed
        pointer-events-none
      `,
      };
    }

    return {
      className: `
      ${baseClasses}
      hover:bg-green-100/60
      active:bg-green-200/40
      hover:scale-[1.02]
    `,
    };
  };

  // const isDateInEventRange = (date: Date, event: any) => {
  //   const startDate = moment(event.start).startOf("day");
  //   const endDate = moment(event.end).startOf("day");
  //   const targetDate = moment(date).startOf("day");
  //   return (
  //     targetDate.isSameOrAfter(startDate) && targetDate.isSameOrBefore(endDate)
  //   );
  // };

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

  const isOverlapping = (
    newStartTime: moment.Moment,
    newEndTime: moment.Moment,
    events: any[]
  ) => {
    for (const event of events) {
      const existingStartTime = moment(event.start);
      const existingEndTime = moment(event.end);

      // 겹침 조건 확인 (여러 가지 방법 중 하나)
      if (
        newStartTime.isBefore(existingEndTime) &&
        newEndTime.isAfter(existingStartTime)
      ) {
        return true; // 겹침 발견
      }
    }
    return false; // 겹치는 예약 없음
  };

  const handleReservation = async () => {
    try {
      if (usageDuration.startsWith("-")) {
        alert("시간 선택이 잘못되었습니다.");
        return;
      }
      if (selectedDates.length === 2 && selectedDates[0] && selectedDates[1]) {
        //selectedDates가 null이 아닐경우
        const startDate = moment(selectedDates[0])
          .format("YYYY-MM-DD")
          .concat(`T${startTime}:00`);
        const endDate = moment(selectedDates[1])
          .format("YYYY-MM-DD")
          .concat(`T${endTime}:00`);

        // Moment 객체로 변환
        const newStartTime = moment(startDate);
        const newEndTime = moment(endDate);

        // 겹침 확인
        if (isOverlapping(newStartTime, newEndTime, events)) {
          alert("선택하신 시간에 이미 예약이 있습니다.");
          return; // 예약 처리 중단
        }

        console.log("uid : ", post.userId);

        const reservationData = {
          postId: post.id,
          renterId: sessionStorage.getItem("user_id"),
          ownerId: post.userId,
          startTime: startDate,
          endTime: endDate,
          deposit: deposit,
          rentalFee: totalPrice,
        };

        console.log("request Data : ", reservationData);

        const response = await fetchWithAuth(
          `${BASE_URL}/api/v1/reservations/request`,
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body: JSON.stringify(reservationData),
          }
        );

        if (response?.ok) {
          const reservation = await response.json();
          console.log("예약 신청 성공:", reservation);
          alert("예약 신청이 완료되었습니다.");

          router.push(
            `./reservation/complete?reservationId=${reservation.data.id}`
          );
        } else {
          const errorData = await response?.json();
          console.error("예약 신청 실패:", errorData);
          alert(`죄송합니다. 이미 예약된 시간입니다.`);
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
    <div className="flex flex-col items-center justify-center min-h-screen bg-green-50 p-8">
      <div className="flex items-center gap-4 pb-6">
        {/* 설명 섹션 - 확대된 배너 */}
        <div className="flex items-center gap-4 bg-gradient-to-r from-green-50 to-green-100 px-5 py-3 rounded-xl border-2 border-green-200 min-w-[240px] hover:shadow-md transition-shadow">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-8 w-8 text-green-700 shrink-0"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
            />
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M12 11v4l-2 2"
              className="text-green-500"
              strokeWidth="2.5"
            />
          </svg>

          <div className="flex flex-col gap-1">
            <h3 className="text-base font-semibold text-gray-700 flex items-center gap-2">
              <span>툴게더 예약 관리</span>
              <span className="bg-green-600 text-white px-2.5 py-1 rounded-full text-sm tracking-tight">
                실시간 동기화
              </span>
            </h3>
            <p className="text-sm text-gray-500">⏰ 예약 가능</p>
          </div>
        </div>
      </div>

      <div className="w-full max-w-6xl mx-4 bg-white rounded-2xl shadow-2xl overflow-hidden transition-all duration-300 hover:shadow-3xl">
        <Calendar
          localizer={localizer}
          startAccessor="start"
          endAccessor="end"
          style={{
            width: "100%",
            height: "calc(100vh - 200px)",
            minHeight: "600px",
            minWidth: "1100px",
            overflow: "hidden",
          }}
          date={date}
          onNavigate={handleNavigate}
          views={{ month: true }}
          messages={messages}
          formats={formats}
          selectable
          onSelectSlot={handleSelectSlot}
          dayPropGetter={dayPropGetter}
          events={events}
          className="text-gray-600 [&_.rbc-header]:bg-green-100 [&_.rbc-today]:bg-green-50"
          components={{
            toolbar: (props) => (
              <div className="flex items-center justify-between px-6 py-4 bg-white border-b border-green-100 shadow-sm">
                <div className="flex items-center gap-5">
                  <h2 className="text-2xl font-bold text-gray-600 tracking-tight">
                    {props.label}
                  </h2>
                  <div className="flex gap-1.5">
                    <button
                      className="p-2.5 text-gray-600 rounded-lg hover:bg-green-50
                    transition-colors duration-200 border border-transparent
                    hover:border-green-200 hover:text-green-700
                    focus:outline-none focus:ring-2 focus:ring-green-500/30"
                      onClick={() => props.onNavigate("PREV")}
                      aria-label="이전 달"
                    >
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="h-5 w-5"
                        viewBox="0 0 20 20"
                        fill="currentColor"
                      >
                        <path
                          fillRule="evenodd"
                          d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z"
                          clipRule="evenodd"
                        />
                      </svg>
                    </button>

                    <button
                      className="px-4 py-2.5 bg-green-500 text-white rounded-lg shadow-sm
                    hover:bg-green-600 transition-colors duration-200
                    focus:outline-none focus:ring-2 focus:ring-green-500/30
                    flex items-center gap-2"
                      onClick={() => props.onNavigate("TODAY")}
                    >
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="h-5 w-5"
                        viewBox="0 0 20 20"
                        fill="currentColor"
                      >
                        <path
                          fillRule="evenodd"
                          d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z"
                          clipRule="evenodd"
                        />
                      </svg>
                      <span className="text-sm font-medium">오늘</span>
                    </button>

                    <button
                      className="p-2.5 text-gray-600 rounded-lg hover:bg-green-50
                    transition-colors duration-200 border border-transparent
                    hover:border-green-200 hover:text-green-700
                    focus:outline-none focus:ring-2 focus:ring-green-500/30"
                      onClick={() => props.onNavigate("NEXT")}
                      aria-label="다음 달"
                    >
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        className="h-5 w-5"
                        viewBox="0 0 20 20"
                        fill="currentColor"
                      >
                        <path
                          fillRule="evenodd"
                          d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                          clipRule="evenodd"
                        />
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            ),
            event: ({ event }) => (
              <div
                className={`p-2 m-1 rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200 
            ${
              event.type === "personal"
                ? "bg-green-500"
                : event.type === "meeting"
                ? "bg-green-400"
                : "bg-green-300"
            } text-white text-sm border-l-4 border-green-700`}
              >
                <div className="font-medium truncate">{event.title}</div>
                {event.desc && (
                  <div className="text-xs mt-1 opacity-90 truncate">
                    {event.desc}
                  </div>
                )}
              </div>
            ),
            week: {
              header: ({ date }) => (
                <div
                  className="text-center py-3 bg-green-50 border-r border-green-100 text-gray-600
                font-medium uppercase text-sm"
                >
                  {date.toLocaleDateString("ko-KR", { weekday: "short" })}
                </div>
              ),
            },
          }}
        />
      </div>

      <div className="flex flex-col mt-10 w-full">
        {/* 날짜 선택 박스 컨테이너 */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 w-full max-w-7xl mx-auto px-8">
          {/* 시작일 섹션 */}
          <div className="bg-green-50 p-8 rounded-xl border-2 border-green-200 shadow-lg">
            <div className="mb-5">
              <h3 className="text-xl font-semibold text-gray-600">시작일</h3>
              <div className="text-base text-gray-500 mt-2">
                {moment(selectedDates[0]).format("YYYY년 MM월 DD일")}
              </div>
            </div>
            <DateBox
              date={selectedDates[0]}
              onTimeChange={handleStartTimeChange}
              time={startTime}
              events={events}
            />
          </div>

          {/* 종료일 섹션 */}
          <div className="bg-green-50 p-8 rounded-xl border-2 border-green-200 shadow-lg">
            <div className="mb-5">
              <h3 className="text-xl font-semibold text-gray-600">종료일</h3>
              <div className="text-base text-gray-500 mt-2">
                {moment(selectedDates[1]).format("YYYY년 MM월 DD일")}
              </div>
            </div>
            <DateBox
              date={selectedDates[1]}
              onTimeChange={handleEndTimeChange}
              time={endTime}
              events={events}
            />
          </div>
        </div>
      </div>

      <div className="flex flex-col items-center mt-10 w-full max-w-2xl mx-auto p-6 bg-green-50 rounded-xl border-2 border-green-200 shadow-sm">
        {/* 안내 문구 섹션 */}
        <div className="w-full mb-8 p-4 bg-green-100 rounded-lg border border-green-200">
          <div className="flex items-center gap-2 text-green-700">
            <span className="text-2xl">✅</span>
            <h3 className="text-lg font-semibold">예약 시 확인사항</h3>
          </div>
          <p className="mt-4 text-gray-600 leading-relaxed">
            당일 혹은 1일 전 예약으로 확정되지 않을 수 있으니
            <br />
            2~3일 전에 예약해주세요.
          </p>
        </div>

        {/* 기간 및 금액 요약 */}
        <div className="w-full space-y-6">
          {/* 기간 표시 */}
          <div className="p-4 bg-white rounded-lg border border-green-200">
            <h4 className="text-gray-500 text-sm mb-2">선택 기간</h4>
            <div className="flex items-center gap-2 text-gray-600">
              <span className="font-medium">
                {moment(selectedDates[0]).format("M/DD(ddd)")} {startTime}
              </span>
              <span className="text-green-500">→</span>
              <span className="font-medium">
                {moment(selectedDates[1]).format("M/DD(ddd)")} {endTime}
              </span>
            </div>
            <div className="mt-2 text-sm text-gray-500">{usageDuration}</div>
          </div>

          {/* 금액 상세 */}
          <div className="space-y-4">
            <div className="flex justify-between items-center">
              <span className="text-gray-600">대여료</span>
              <span className="font-medium">
                {totalPrice.toLocaleString()}원
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-600">보증금</span>
              <span className="font-medium">{deposit.toLocaleString()}원</span>
            </div>
            <hr className="border-t-2 border-green-200" />
            <div className="flex justify-between items-center text-lg font-bold text-green-700">
              <span>총 결제 금액</span>
              <span>{(totalPrice + deposit).toLocaleString()}원</span>
            </div>
          </div>

          {/* 예약 버튼 */}
          <button
            className="w-full py-4 bg-green-600 hover:bg-green-700 text-white font-bold rounded-lg
                 transition-all duration-200 transform hover:scale-[1.02] shadow-md hover:shadow-lg"
            onClick={handleReservation}
          >
            동의하고 예약 신청하기
          </button>
        </div>
      </div>
    </div>
  );
}
