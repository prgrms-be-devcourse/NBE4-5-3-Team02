"use client";

import { Calendar, momentLocalizer } from "react-big-calendar";
import moment from "moment";
import "moment/locale/ko";
import "react-big-calendar/lib/css/react-big-calendar.css";
import { useMemo, useState, useEffect } from "react";
import ScoreIcon from "../lib/util/scoreIcon";
import Link from "next/link";
import Image from "next/image";
import { fetchWithAuth } from "../lib/util/fetchWithAuth";
import { useRouter } from "next/navigation";
import {ArrowDownTrayIcon, ArrowUpTrayIcon, DocumentMagnifyingGlassIcon, PencilSquareIcon, PhotoIcon} from "@heroicons/react/16/solid";
import {PhoneIcon} from "@heroicons/react/24/solid";
import {
  CalendarIcon,
  CameraIcon, CheckCircleIcon,
  CheckIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  ClockIcon,
  CreditCardIcon,
  MapPinIcon,
  StarIcon,
  TrashIcon
} from "lucide-react";
import {UserCircleIcon} from "@heroicons/react/24/outline";
import {ArrowRightOnRectangleIcon} from "@heroicons/react/20/solid";

interface Me {
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

interface Rental {
  id: number;
  title: string;
  image: string;
  amount: number;
  startTime: string;
  endTime: string;
  status: string;
  isReviewed: boolean;
}

interface Borrow {
  id: number;
  title: string;
  image: string;
  amount: number;
  startTime: string;
  endTime: string;
  status: string;
  isReviewed: boolean;
}

interface Reservations {
  rentals: Rental[];
  borrows: Borrow[];
}

interface InfoCardProps {
  icon: React.ReactNode;
  title: string;
  value: React.ReactNode;
  color: string;
}

export default function ClientPage() {
  const [me, setMe] = useState<Me>({
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
  const [reservations, setReservations] = useState<Reservations>({
    rentals: [],
    borrows: [],
  });
  const [eventType, setEventType] = useState("rental");
  const [date, setDate] = useState(new Date());
  const [isModal, setIsModal] = useState(false);
  const [modalTitle, setModalTitle] = useState("");
  const [modalContent, setModalContent] = useState("");
  const [onConfirmModal, setOnConfirmModal] = useState<(() => void) | null>(
    () => {}
  );
  const [isCancelButton, setIsCancelButton] = useState(true);

  const localizer = momentLocalizer(moment);
  const router = useRouter();
  const BASE_URL = process.env.NEXT_PUBLIC_BASE_URL;

  useEffect(() => {
    getMe();
    getReservations();
  }, []);

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
    rentals: reservations?.rentals.filter(
        (rental) =>
            rental.status === "APPROVED" || rental.status === "IN_PROGRESS"
    ),
    borrows: reservations?.borrows.filter(
        (borrow) =>
            borrow.status === "APPROVED" || borrow.status === "IN_PROGRESS"
    ),
  };
  const rentalEvents = scheduleReservations.rentals.map((rental) => ({
    id: rental.id,
    title: `빌려주기: ${rental.title}`,
    start: moment(rental.startTime).toDate(),
    end: moment(rental.endTime).toDate(),
    color: getRandomColor(),
  }));
  const borrowEvents = scheduleReservations.borrows.map((borrow) => ({
    id: borrow.id,
    title: `빌리기: ${borrow.title}`,
    start: new Date(borrow.startTime),
    end: new Date(borrow.endTime),
    color: getRandomColor(),
  }));

  // const messages = {
  //   today: "오늘",
  //   previous: "이전",
  //   next: "다음",
  //   month: "월",
  //   week: "주",
  //   day: "일",
  //   agenda: "일정",
  //   date: "날짜",
  //   time: "시간",
  //   event: "이벤트",
  // };

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

  // const formats = {
  //   dateFormat: "D",
  //   dayFormat: (date: Date, culture: any, localizer: any) =>
  //       localizer.format(date, "dddd", culture),
  //   weekdayFormat: (date: Date, culture: any, localizer: any) =>
  //       localizer.format(date, "ddd", culture),
  //   monthHeaderFormat: (date: Date, culture: any, localizer: any) =>
  //       localizer.format(date, "YYYY년 MM월", culture),
  // };

  const filteredReservations = useMemo(() => {
    if (eventType === "rental") {
      return reservations?.rentals;
    } else {
      return reservations?.borrows;
    }
  }, [eventType, reservations]);

  const handleModal = (
      title: string,
      content: string,
      confirmAction: (() => void) | null = null,
      isCancelButton: boolean = true
  ) => {
    setModalTitle(title);
    setModalContent(content);
    setIsModal(true);
    setOnConfirmModal(() => confirmAction);
    setIsCancelButton(isCancelButton);
  };

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
      if (Data?.code.startsWith("403")) {
        router.push("/login");
      }
      if (Data?.code !== "200-1") {
        console.error(`에러가 발생했습니다. \n${Data?.msg}`);
      }
      setMe(Data?.data);
    } else {
      if (getMyInfo?.status === 403) {
        router.push("/login");
      }
      console.error("Error fetching data:", getMyInfo?.status);
    }
  };

  //예약정보 조회
  const getReservations = async () => {
    const getMyReservations = await fetchWithAuth(
      `${BASE_URL}/api/v1/mypage/reservations`,
      {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (getMyReservations?.ok) {
      const Data = await getMyReservations.json();
      if (Data?.code !== "200-1") {
        console.error(`에러가 발생했습니다. \n${Data?.msg}`);
      }
      setReservations(Data?.data);
    } else {
      console.error("Error fetching data:", getMyReservations?.status);
    }
  };

  //폼 데이터를 서버로 전송하는 함수
  const handleUploadProfile = async (
      e: React.ChangeEvent<HTMLInputElement>
  ) => {
    const file = e.target.files?.[0];

    if (!file) {
      alert("이미지를 선택해주세요.");
      return;
    }

    const formData = new FormData();
    formData.append("profileImage", file);

    try {
      const uploadProfile = await fetchWithAuth(
        `${BASE_URL}/api/v1/mypage/profile`,
        {
          method: "POST",
          credentials: "include",
          body: formData,
        }
      );

      if (uploadProfile?.ok) {
        const Data = await uploadProfile.json();
        if (Data?.code.startsWith("200")) {
          handleModal(
            "성공",
            "프로필이 수정되었습니다.",
            () => {
              window.location.reload();
            },
            false
          );
        } else {
          handleModal("프로필 수정 실패", Data?.msg, null, false);
        }
      } else {
        console.error("Error fetching data:", uploadProfile?.status);
        handleModal(
          "프로필 수정 실패",
          `오류가 발생했습니다. (HTTP 상태 코드: ${uploadProfile?.status})`,
          null,
          false
        );
      }
    } catch (error) {
      console.error("프로필 수정 중 오류 발생:", error);
      handleModal(
        "프로필 수정 실패",
        "프로필 수정 중 예상치 못한 오류가 발생했습니다.",
        null,
        false // 실패 모달 (취소 버튼 숨김)
      );
    }
  };

  const handleDeleteProfile = async () => {
    try {
      const deleteProfile = await fetchWithAuth(
        `${BASE_URL}/api/v1/mypage/profile`,
        {
          method: "DELETE",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      if (deleteProfile?.ok) {
        const Data = await deleteProfile.json();
        if (Data?.code.startsWith("200")) {
          handleModal(
            "성공",
            "프로필이 삭제되었습니다.",
            () => {
              window.location.reload();
            },
            false
          );
        } else {
          handleModal("프로필 삭제 실패", Data?.msg, null, false);
        }
      } else {
        console.error("Error fetching data:", deleteProfile?.status);
        handleModal(
          "프로필 삭제 실패",
          `오류가 발생했습니다. (HTTP 상태 코드: ${deleteProfile?.status})`,
          null,
          false
        );
      }
    } catch (error) {
      console.error("프로필 삭제 중 오류 발생:", error);
      handleModal(
        "프로필 삭제 실패",
        "프로필 삭제 중 예상치 못한 오류가 발생했습니다.",
        null,
        false
      );
    }
  };

  const handleWithdrawMembership = async () => {
    try {
      const withdrawMembership = await fetchWithAuth(
        `${BASE_URL}/api/v1/mypage/me`,
        {
          method: "DELETE",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      if (withdrawMembership?.ok) {
        const Data = await withdrawMembership.json();
        if (Data?.code.startsWith("200")) {
          handleModal(
            "회원 탈퇴 완료",
            "회원 탈퇴가 정상적으로 처리되었습니다.",
            () => {
              window.location.href = "/";
            },
            false
          );
        } else {
          handleModal(
            "회원 탈퇴 실패",
            Data?.msg || "회원 탈퇴에 실패했습니다.",
            null,
            false
          );
        }
      } else {
        console.error("회원 탈퇴 API 요청 실패:", withdrawMembership?.status);
        handleModal(
          "회원 탈퇴 실패",
          `오류가 발생했습니다. (HTTP 상태 코드: ${withdrawMembership?.status})`,
          null,
          false
        );
      }
    } catch (error) {
      console.error("회원 탈퇴 처리 중 오류 발생:", error);
      handleModal(
        "회원 탈퇴 실패",
        "회원 탈퇴 처리 중 예상치 못한 오류가 발생했습니다.",
        null,
        false
      );
    }
  };

  

  const InfoCard = ({ icon, title, value, color }: InfoCardProps) => (
      <div className={`${color} p-4 rounded-xl flex items-center gap-3 transition-transform hover:scale-[1.02]`}>
        <div className="p-2 bg-white rounded-lg shadow-sm">{icon}</div>
        <div>
          <p className="text-xs text-gray-500">{title}</p>
          <p className="text-gray-600 font-semibold">{value}</p>
        </div>
      </div>
  );
  const TabButton = ({ active, children, onClick, icon }: { active: boolean; children: React.ReactNode; onClick: () => void; icon: React.ReactNode }) => (
      <button
          onClick={onClick}
          className={`flex-1 py-3 text-center font-semibold flex items-center justify-center gap-2 transition-all
      ${
              active
                  ? 'text-emerald-700 border-b-2 border-emerald-600'
                  : 'text-gray-600 hover:text-emerald-600 hover:bg-emerald-50'
          }`}
      >
        {icon}
        <span className="text-lg">{children}</span>
      </button>
  );

  const ReservationCard = ({ reservation }: { reservation: Rental | Borrow }) => (
      <Link
          href={`/mypage/reservationDetail/${reservation.id}`}
          className="group block bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow"
      >
        <div className="flex p-4 gap-4 items-start">
          {/* 이미지 섹션 */}
          <div className="relative w-24 h-24 flex-shrink-0">
            <Image
                src={reservation.image}
                alt={reservation.title}
                fill
                className="object-cover rounded-lg"
                sizes="(max-width: 768px) 100vw, 25vw"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/30 rounded-lg" />
          </div>

          {/* 정보 섹션 */}
          <div className="flex-1">
            <div className="flex items-center gap-3 mb-2">
              <h3 className="text-gray-600 font-semibold text-lg">
                {reservation.title}
              </h3>
              <span className={`px-2 py-1 rounded-full text-sm ${
                  reservationStatusStyle[reservation.status as 'pending' | 'confirmed' | 'completed' | 'canceled']?.bg
                  || 'bg-gray-200 text-gray-600'
              }`}>
            {reservationStatus[reservation.status]}
          </span>
            </div>

            {/* 타임라인 */}
            <div className="grid grid-cols-2 gap-3 text-gray-600">
              <InfoItem
                  icon={<ClockIcon className="w-5 h-5" />}
                  label="대여 시간"
                  value={moment(reservation.startTime).format("MM/DD HH:mm")}
              />
              <InfoItem
                  icon={<ClockIcon className="w-5 h-5" />}
                  label="반납 시간"
                  value={moment(reservation.endTime).format("MM/DD HH:mm")}
              />
            </div>

            {/* 가격 & 상태 */}
            <div className="mt-3 flex items-center gap-4">
              <p className="text-emerald-600 font-bold">
                {reservation.amount.toLocaleString()}원
              </p>
              <ChevronRightIcon className="w-5 h-5 text-gray-400 group-hover:translate-x-1 transition-transform" />
            </div>
          </div>
        </div>
      </Link>
  );

  const InfoItem = ({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) => (
      <div className="flex items-center gap-2">
        <div className="text-emerald-600">{icon}</div>
        <div>
          <p className="text-sm text-gray-500">{label}</p>
          <p className="text-gray-600 font-medium">{value}</p>
        </div>
      </div>
  );

// 상태 스타일 맵핑
  const reservationStatusStyle: { [key in 'pending' | 'confirmed' | 'completed' | 'canceled']: { bg: string } } = {
    pending: { bg: 'bg-amber-100 text-amber-800' },
    confirmed: { bg: 'bg-emerald-100 text-emerald-800' },
    completed: { bg: 'bg-blue-100 text-blue-800' },
    canceled: { bg: 'bg-red-100 text-red-800' }
  };

  // const CustomToolbar = ({ label, date, view, views, onView, onNavigate }: { label: string; date: Date; view: string; views: string[]; onView: (view: string) => void; onNavigate: (action: string) => void }) => {
  //   const navigate = (action: string) => {
  //     onNavigate(action);
  //   };

  //   const viewNames = {
  //     month: '월별',
  //     week: '주별',
  //     day: '일별',
  //     agenda: '일정 목록'
  //   };

  //   return (
  //       <div className="flex items-center justify-between mb-4 p-3 bg-emerald-50 rounded-lg">
  //         {/* 네비게이션 컨트롤 */}
  //         <div className="flex gap-2">
  //           <button
  //               className="p-2 rounded-lg bg-white text-emerald-600 hover:bg-emerald-100 transition-colors"
  //               onClick={() => navigate('PREV')}
  //           >
  //             <ChevronLeftIcon className="w-5 h-5" />
  //           </button>
  //           <button
  //               className="p-2 rounded-lg bg-white text-emerald-600 hover:bg-emerald-100 transition-colors"
  //               onClick={() => navigate('TODAY')}
  //           >
  //             오늘
  //           </button>
  //           <button
  //               className="p-2 rounded-lg bg-white text-emerald-600 hover:bg-emerald-100 transition-colors"
  //               onClick={() => navigate('NEXT')}
  //           >
  //             <ChevronRightIcon className="w-5 h-5" />
  //           </button>
  //         </div>

  //         {/* 현재 날짜 표시 */}
  //         <span className="text-lg font-semibold text-gray-600">
  //       {label}
  //     </span>

  //         {/* 뷰 선택 버튼 */}
  //         <div className="flex gap-2">
  //           {views.map((name) => (
  //               <button
  //                   key={name}
  //                   className={`px-4 py-2 rounded-lg transition-colors ${
  //                       view === name
  //                           ? 'bg-emerald-600 text-white'
  //                           : 'bg-white text-emerald-600 hover:bg-emerald-50'
  //                   }`}
  //                   onClick={() => onView(name)}
  //               >
  //                 {viewNames[name]}
  //               </button>
  //           ))}
  //         </div>
  //       </div>
  //   );
  // };

  // const CustomEvent = ({ event }) => (
  //     <div className="flex items-start p-1 group">
  //       <div className="flex-1">
  //         <div className="flex items-center gap-2">
  //           <div className="w-2 h-2 rounded-full bg-white" />
  //           <span className="font-medium truncate">{event.title}</span>
  //         </div>
  //         {event.desc && (
  //             <p className="text-xs opacity-80 mt-1 truncate">{event.desc}</p>
  //         )}
  //       </div>
  //       <ChevronRightIcon className="w-4 h-4 opacity-0 group-hover:opacity-100 transition-opacity" />
  //     </div>
  // );

  return (
      <div className="min-h-screen bg-gradient-to-b from-emerald-50 to-green-50 p-4 md:p-8">
        <div className="max-w-4xl mx-auto space-y-6">

          <div className="flex justify-between items-center">
            {/* 제목 영역 */}
            <div className="space-y-1">
              <h1 className="text-3xl font-bold text-gray-600 tracking-tight">
                마이페이지
              </h1>
              <p className="text-gray-500 text-sm">최근 업데이트: 3시간 전</p>
            </div>

            {/* 수정 버튼 영역 */}
            <Link
                href="/mypage/edit"
                className="group flex items-center space-x-2 bg-emerald-600 hover:bg-emerald-700 text-white px-5 py-3 rounded-lg transition-all duration-200 ease-out hover:translate-y-[-2px] active:translate-y-0 shadow-md hover:shadow-emerald-200"
            >
              <svg
                  xmlns="http://www.w3.org/2000/svg"
                  className="h-5 w-5 group-hover:rotate-12 transition-transform"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
              >
                <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"
                />
              </svg>
              <span className="font-semibold">수정하기</span>
            </Link>
          </div>

          {/* 구분선 */}
          <div className="my-6 border-t border-emerald-100/60" />


          <div className="grid grid-cols-1 gap-4 mt-4">
            <div className="shadow-2xl p-8 bg-gradient-to-br from-green-50 to-green-100 rounded-2xl grid grid-cols-1 lg:grid-cols-[1.5fr_1fr] gap-8 transition-all duration-300 hover:shadow-3xl">
              {/* 유저 정보 섹션 */}
              <div className="space-y-6">
                <h2 className="text-2xl font-bold text-gray-600 flex items-center gap-2">
                  <UserCircleIcon className="w-8 h-8 text-emerald-600" />
                  나의 정보
                </h2>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {/* 정보 카드 그룹 */}
                  <div className="col-span-2 bg-white p-6 rounded-xl shadow-sm border border-green-100">
                    <div className="space-y-4">
                      {/* 개별 정보 항목 */}
                      <div className="flex justify-between items-center group hover:bg-green-50 rounded-lg p-2 transition-colors">
                        <div>
                          <p className="text-sm text-gray-400 font-medium">닉네임</p>
                          <p className="text-gray-600 font-semibold">{me?.nickname}</p>
                        </div>
                        <PencilSquareIcon
                            className="w-5 h-5 text-gray-400 opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer"
                        />
                      </div>

                      {/* 연락처 정보 */}
                      <div className="flex items-center gap-3 p-2">
                        <PhoneIcon className="w-5 h-5 text-emerald-600 shrink-0" />
                        <div>
                          <p className="text-sm text-gray-400">연락처</p>
                          <p className="text-gray-600">{me?.phoneNumber || '미등록'}</p>
                        </div>
                      </div>

                      {/* 주소 정보 */}
                      <div className="group relative p-2 rounded-lg hover:bg-green-50 transition-colors">
                        <div className="flex items-center gap-3">
                          <MapPinIcon className="w-5 h-5 text-emerald-600 shrink-0" />
                          <div className="truncate">
                            <p className="text-sm text-gray-400">주소</p>
                            <p className="text-gray-600">
                              {me?.address.mainAddress}
                              <span className="block text-sm">{me?.address.detailAddress}</span>
                            </p>
                          </div>
                        </div>
                        <button className="absolute right-2 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 transition-opacity text-emerald-600 text-sm">
                          변경
                        </button>
                      </div>
                    </div>
                  </div>

                  {/* 부가 정보 그리드 */}
                  <div className="grid grid-cols-1 gap-3">
                    <InfoCard
                        icon={<CalendarIcon className="w-6 h-6" />}
                        title="가입일"
                        value={localizer.format(me?.createdAt, "YYYY년 MM월 DD일", "ko")}
                        color="bg-emerald-100"
                    />

                    <InfoCard
                        icon={<StarIcon className="w-6 h-6" />}
                        title="평점"
                        value={
                          <div className="flex items-center justify-end gap-2 w-full"> {/* gap 확대 */}
                            <span className="text-lg font-bold text-amber-700">{me?.score}</span> {/* 텍스트 강조 */}
                            <div className="rounded-full bg-amber-200 p-[6px] overflow-hidden w-9 h-9 shadow-sm">
                              {/* 크기 조정: w-9 h-9 */}
                              <ScoreIcon
                                  score={me?.score}
                                  className="w-full h-full object-cover transform hover:scale-110 transition-transform"
                                  size={32}
                              />
                            </div>
                          </div>
                        }
                        color="bg-amber-100"
                    />

                    <InfoCard
                        icon={<CreditCardIcon className="w-6 h-6" />}
                        title="크레딧"
                        value={`${me?.credit?.toLocaleString()} P`}
                        color="bg-blue-100"
                    />
                  </div>
                </div>
              </div>

              {/* 프로필 섹션 */}
              <div className="flex flex-col items-center justify-center gap-6">
                <div className="relative group">
                  <div className={`w-48 h-48 rounded-full shadow-lg border-4 border-emerald-100 transition-all duration-300 
        ${me?.profileImage ? 'hover:scale-105 hover:border-emerald-200' : 'border-dashed animate-pulse'}`}>
                    {me?.profileImage ? (
                        <>
                          <Image
                              src={me.profileImage}
                              alt="Profile"
                              fill
                              className="object-cover rounded-full"
                              sizes="(max-width: 768px) 100vw, 50vw"
                          />
                          <div className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 rounded-full flex items-center justify-center gap-4 transition-opacity">
                            <CameraIcon
                                className="w-8 h-8 text-white cursor-pointer hover:text-emerald-300 transition-colors"
                                onClick={() => document.getElementById('profile-upload-input')?.click()}
                            />
                            <TrashIcon
                                className="w-8 h-8 text-white cursor-pointer hover:text-red-400 transition-colors"
                                onClick={() => handleModal("프로필 삭제", "프로필을 삭제하시겠습니까?", handleDeleteProfile)}
                            />
                          </div>
                        </>
                    ) : (
                        <button
                            className="w-full h-full flex flex-col items-center justify-center text-emerald-600 hover:text-emerald-700"
                            onClick={() => document.getElementById('profile-upload-input')?.click()}
                        >
                          <PhotoIcon className="w-16 h-16 mb-2" />
                          <span className="font-medium">프로필 사진 추가</span>
                        </button>
                    )}
                  </div>
                </div>

                {/* 상태 표시 바 */}
                <div className="w-full max-w-[200px] bg-emerald-600 rounded-full h-3">  {/* 커스텀 최대 너비 */}
                  <div
                      className="bg-emerald-600 h-0 rounded-full transition-all duration-500"
                      style={{ width: `${(me?.credit / 1000) * 100}%` }}
                  />
                </div>
                <p className="text-sm text-gray-500">다음 등급까지 {1000 - (me?.credit % 1000)}P 남았습니다</p>
              </div>
            </div>

            {/* 예약 현황 */}
            <div className="bg-gradient-to-br from-green-50 to-green-100 shadow-xl rounded-2xl p-6">
              {/* 헤더 및 탭 버튼 */}
              <div className="mb-8">
                <h2 className="text-2xl font-bold text-gray-600 flex items-center gap-2 mb-6">
                  <CalendarIcon className="w-6 h-6 text-emerald-600" />
                  예약 관리
                </h2>

                <div className="flex border-b-2 border-emerald-100">
                  <TabButton
                      active={eventType === "borrow"}
                      onClick={() => setEventType("borrow")}
                      icon={<ArrowUpTrayIcon className="w-5 h-5" />}
                  >
                    나의 예약 요청
                  </TabButton>
                  <TabButton
                      active={eventType === "rental"}
                      onClick={() => setEventType("rental")}
                      icon={<ArrowDownTrayIcon className="w-5 h-5" />}
                  >
                    예약 대기 목록
                  </TabButton>
                </div>
              </div>

              {/* 캘린더 섹션 */}
              <div className="bg-gradient-to-br from-emerald-50 to-green-50 rounded-2xl shadow-lg border border-emerald-100 p-6">
                <Calendar
                    localizer={localizer}
                    events={eventType === "rental" ? rentalEvents : borrowEvents}
                    startAccessor="start"
                    endAccessor="end"
                    date={date}
                    onNavigate={handleNavigate}
                    style={{ height: '600px' }}
                    eventPropGetter={(event) => ({
                      style: {
                        backgroundColor: event.color,
                        //@ts-expect-error: error from mypage clientChange calender border color
                        border: `2px solid ${event.borderColor}`,
                        borderRadius: '8px',
                        color: "white",
                        boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                        padding: '4px 8px',
                        fontSize: '0.875rem'
                      },
                    })}
                    formats={{
                      dayFormat: 'D',
                      monthHeaderFormat: 'YYYY년 MM월',
                      dayHeaderFormat: 'M/D (ddd)',
                      timeGutterFormat: 'HH:mm'
                    }}
                    messages={{
                      today: '오늘',
                      previous: '←',
                      next: '→',
                      month: '월별',
                      week: '주별',
                      day: '일별',
                      agenda: '일정 목록'
                    }}
                    components={{
                      toolbar: (props) => (
                          <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
                            <div className="flex items-center gap-2">
                              <button
                                  className="rounded-lg bg-emerald-600 px-4 py-2 text-white hover:bg-emerald-700"
                                  onClick={() => props.onView('month')}
                              >
                                월별
                              </button>
                              <button
                                  className="rounded-lg bg-emerald-100 px-4 py-2 text-emerald-700 hover:bg-emerald-200"
                                  onClick={() => props.onView('agenda')}
                              >
                                목록 보기
                              </button>
                            </div>
                            <span className="text-xl font-bold text-gray-800">
            {moment(props.date).format('YYYY년 MM월')}
          </span>
                            <div className="flex gap-2">
                              <button
                                  className="rounded-lg p-2 hover:bg-emerald-100"
                                  onClick={() => props.onNavigate('PREV')}
                              >
                                ←
                              </button>
                              <button
                                  className="rounded-lg p-2 hover:bg-emerald-100"
                                  onClick={() => props.onNavigate('TODAY')}
                              >
                                오늘
                              </button>
                              <button
                                  className="rounded-lg p-2 hover:bg-emerald-100"
                                  onClick={() => props.onNavigate('NEXT')}
                              >
                                →
                              </button>
                            </div>
                          </div>
                      ),
                      event: ({ event }) => (
                          <div className="flex items-start">
                            <div className="mr-2 mt-1 h-2 w-2 rounded-full"
                        //@ts-expect-error: error from mypage clientChange calender border color
                                 style={{ backgroundColor: event.borderColor }} />
                            <div>
                              <p className="font-medium">{event.title}</p>
                              <p className="text-xs opacity-80">
                                {moment(event.start).format('HH:mm')}~
                                {moment(event.end).format('HH:mm')}
                              </p>
                            </div>
                          </div>
                      )
                    }}
                    dayPropGetter={(date) => ({
                      className: moment(date).isSame(new Date(), 'day')
                          ? 'bg-emerald-100/50'
                          : ''
                    })}
                    className="[&_.rbc-header]:bg-emerald-100 [&_.rbc-header]:py-3 [&_.rbc-header]:text-emerald-800 [&_.rbc-day-bg+.rbc-day-bg]:border-l-emerald-50"
                />
              </div>

              {/* 예약 목록 */}
              <div className="space-y-4">
                {filteredReservations.length > 0 ? (
                    filteredReservations.map((reservation) => (
                        <ReservationCard
                            key={reservation.id}
                            reservation={reservation}
                            //@ts-expect-error: error in mypage reservationList
                            eventType={eventType}
                        />
                    ))
                ) : (
                    <div className="bg-emerald-50 p-6 rounded-xl text-center">
                      <DocumentMagnifyingGlassIcon className="w-12 h-12 text-emerald-400 mx-auto mb-4" />
                      <p className="text-gray-600 font-medium">등록된 예약이 없습니다</p>
                    </div>
                )}
              </div>
            </div>

            {/* 회원 탈퇴 버튼 추가 */}
            <div className="flex justify-end mt-8">
              <button
                  onClick={() =>
                      handleModal(
                          "회원 탈퇴",
                          "정말로 회원 탈퇴하시겠습니까?",
                          handleWithdrawMembership,
                          true
                      )
                  }
                  className="flex items-center gap-2 rounded-lg border border-emerald-600 px-4 py-2 text-gray-600 transition-colors hover:bg-emerald-50 hover:shadow-sm"
              >
                <ArrowRightOnRectangleIcon className="h-5 w-5 text-emerald-600" />
                <span className="font-medium">회원 탈퇴</span>
              </button>
            </div>

          </div>
        </div>

        {/* 성공 모달 */}
        {isModal && (
            <div className="fixed inset-0 z-50 bg-emerald-100/80 backdrop-blur-sm transition-opacity">
              <div className="flex min-h-screen items-center justify-center p-4 text-center">
                <div
                    className="relative transform overflow-hidden rounded-2xl bg-white shadow-2xl transition-all sm:my-8 sm:w-full sm:max-w-lg"
                    role="dialog"
                    aria-modal="true"
                    aria-labelledby="modal-headline"
                >
                  {/* 컨텐츠 영역 */}
                  <div className="bg-emerald-50 p-6 sm:p-8">
                    <div className="flex flex-col items-center space-y-4">
                      {/* 아이콘 배지 */}
                      <div className="flex h-12 w-12 items-center justify-center rounded-full bg-emerald-600">
                        <CheckIcon className="h-8 w-8 text-white" />
                      </div>

                      {/* 텍스트 컨텐츠 */}
                      <div className="text-center">
                        <h3 className="text-xl font-bold text-gray-600">
                          {modalTitle}
                        </h3>
                        <p className="mt-2 text-gray-600">
                          {modalContent}
                        </p>
                      </div>
                    </div>
                  </div>

                  {/* 액션 버튼 그룹 */}
                  <div className="grid grid-cols-1 gap-3 bg-white p-6 sm:grid-cols-2">
                    {isCancelButton && (
                        <button
                            onClick={() => setIsModal(false)}
                            className="rounded-lg border border-gray-300 bg-white px-4 py-3 text-gray-600 transition-colors hover:bg-gray-50"
                        >
                          취소
                        </button>
                    )}
                    <button
                        onClick={() => {
                          onConfirmModal?.()
                          setIsModal(false)
                        }}
                        className="flex items-center justify-center gap-2 rounded-lg bg-emerald-600 px-4 py-3 text-white transition-colors hover:bg-emerald-700"
                    >
                      <CheckCircleIcon className="h-5 w-5" />
                      확인
                    </button>
                  </div>
                </div>
              </div>
            </div>
        )}

        {/* 이미지 선택 input (숨김) */}
        <input
            type="file"
            id="profile-upload-input"
            accept="image/*" // 이미지 파일만 허용
            className="hidden" // 숨김 처리
            onChange={handleUploadProfile} // 이미지 선택 시 이벤트 핸들러 호출
        />
      </div>
  );
}