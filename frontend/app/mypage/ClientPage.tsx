"use client";

import { Calendar, momentLocalizer } from "react-big-calendar";
import moment from "moment";
import "moment/locale/ko";
import "react-big-calendar/lib/css/react-big-calendar.css";
import { useMemo, useState, useEffect } from "react";
import ScoreIcon from "../lib/util/scoreIcon";
import Link from "next/link";
import Image from "next/image";

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
  })
  const [reservations, setReservations] = useState<Reservations>({
    rentals: [
      {
        id: 0,
        title: "",
        image: "",
        amount: 0,
        startTime: "",
        endTime: "",
        status: "",
        isReviewed: false,
      },
    ],
    borrows: [
      {
        id: 0,
        title: "",
        image: "",
        amount: 0,
        startTime: "",
        endTime: "",
        status: "",
        isReviewed: false,
      },
    ],
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
      setMe(Data?.data);
    } else {
      console.error("Error fetching data:", getMyInfo.status);
    }
  };

  //예약정보 조회
  const getReservations = async () => {
    const getMyReservations = await fetch(
      "http://localhost:8080/api/v1/mypage/reservations",
      {
        method: "GET",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (getMyReservations.ok) {
      const Data = await getMyReservations.json();
      if (Data?.code !== "200-1") {
        console.error(`에러가 발생했습니다. \n${Data?.msg}`);
      }
      setReservations(Data?.data);
    } else {
      console.error("Error fetching data:", getMyReservations.status);
    }
  }

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
      const uploadProfile = await fetch(
        "http://localhost:8080/api/v1/mypage/profile",
        {
          method: "POST",
          credentials: "include",
          body: formData,
        }
      );

      if (uploadProfile.ok) {
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
        console.error("Error fetching data:", uploadProfile.status);
        handleModal(
          "프로필 수정 실패",
          `오류가 발생했습니다. (HTTP 상태 코드: ${uploadProfile.status})`,
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
      const deleteProfile = await fetch(
        "http://localhost:8080/api/v1/mypage/profile",
        {
          method: "DELETE",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      if (deleteProfile.ok) {
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
        console.error("Error fetching data:", deleteProfile.status);
        handleModal(
          "프로필 삭제 실패",
          `오류가 발생했습니다. (HTTP 상태 코드: ${deleteProfile.status})`,
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
      const withdrawMembership = await fetch(
        "http://localhost:8080/api/v1/mypage/me",
        {
          method: "DELETE",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      if (withdrawMembership.ok) {
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
        console.error("회원 탈퇴 API 요청 실패:", withdrawMembership.status);
        handleModal(
          "회원 탈퇴 실패",
          `오류가 발생했습니다. (HTTP 상태 코드: ${withdrawMembership.status})`,
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
          <div className="shadow-md p-4 bg-white grid grid-cols-2 gap-4">
            {/* 유저 정보 */}
            <div className="">
              <h2 className="text-lg font-bold text-gray-800">나의 정보</h2>
              <div className="mt-4">
                <p className="text-gray-800">
                  <span className="font-bold">닉네임: </span>
                  {me?.nickname}
                </p>
                <p className="text-gray-800">
                  <span className="font-bold">전화번호: </span>
                  {me?.phoneNumber}
                </p>
                {me?.username ? (
                  <p className="text-gray-800">
                    <span className="font-bold">아이디: </span> {me?.username}
                  </p>
                ) : (
                  <p className="text-gray-800">
                    <span className="font-bold">이메일: </span> {me?.email}
                  </p>
                )}
                <p className="text-gray-800">
                  <span className="font-bold">주소: </span>{" "}
                  {me?.address.mainAddress} {me?.address.detailAddress} (
                  {me?.address.zipcode})
                </p>
                <p className="text-gray-800">
                  <span className="font-bold">가입일:</span>{" "}
                  {localizer.format(me?.createdAt, "YYYY년 MM월 DD일", "ko")}
                </p>
                <p className="text-gray-800">
                  <span className="flex flex-row">
                    <span className="font-bold">평점: </span>
                    {me?.score}
                    <ScoreIcon
                      className="ml-2"
                      score={me?.score}
                      size={25}
                      round
                    />
                  </span>
                </p>
                <p className="text-gray-800">
                  <span className="font-bold">크레딧: </span> {me?.credit}
                </p>
              </div>
            </div>
            {/* 프로필 */}
            <div className="flex flex-col items-center justify-center">
              <div className="relative">
                {!me?.profileImage ? (
                  <div className="w-30 h-30 bg-gray-200 rounded-full overflow-hidden border-2 border-gray-300 flex items-center justify-center">
                    {/* 프로필 추가 버튼 (프로필 이미지 없을 때) */}
                    <button
                      className="text-gray-600 hover:text-gray-800"
                      onClick={() =>
                        document.getElementById("profile-upload-input")?.click()
                      }
                    >
                      프로필 추가
                    </button>
                  </div>
                ) : (
                  <div className="relative w-30 h-30 rounded-full overflow-hidden border-2 border-gray-300">
                    <Image
                      src={me?.profileImage}
                      alt="profile"
                      fill
                      sizes="100%"
                      style={{ objectFit: "cover" }}
                    />
                    {/* 프로필 수정 및 삭제 버튼 (프로필 이미지 있을 때) */}
                    <div className="absolute bottom-0 left-0 right-0 flex justify-around bg-gray-100 bg-opacity-75 p-1">
                      <button
                        className="text-blue-500 hover:text-blue-700"
                        onClick={() =>
                          document
                            .getElementById("profile-upload-input")
                            ?.click()
                        }
                      >
                        수정
                      </button>
                      <button
                        className="text-red-500 hover:text-red-700"
                        onClick={() =>
                          handleModal(
                            "프로필 삭제",
                            "프로필을 삭제하시겠습니까?",
                            handleDeleteProfile
                          )
                        }
                      >
                        삭제
                      </button>
                    </div>
                  </div>
                )}
              </div>
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
                      <Image
                        src={reservation.image}
                        alt={reservation.title}
                        className="w-20 h-20 object-cover rounded mr-4"
                        width={80}
                        height={80}
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
          {/* 회원 탈퇴 버튼 추가 */}
          <div className="flex justify-end">
            <button
              className="py-2 px-4"
              onClick={() =>
                handleModal(
                  "회원 탈퇴",
                  "정말로 회원 탈퇴하시겠습니까?",
                  handleWithdrawMembership,
                  true
                )
              }
            >
              회원 탈퇴
            </button>
          </div>
        </div>
      </div>

      {/* 성공 모달 */}
      {isModal && (
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
                      {modalTitle}
                    </h3>
                    <div className="mt-2">
                      <p className="text-sm text-gray-500">{modalContent}</p>
                    </div>
                  </div>
                </div>
              </div>
              <div className="bg-gray-50 px-4 py-3 sm:px-6 sm:flex sm:flex-row-reverse">
                <button
                  type="button"
                  className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-blue-500 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
                  onClick={() => {
                    if (onConfirmModal) {
                      onConfirmModal();
                    }
                    setIsModal(false);
                  }}
                >
                  확인
                </button>
                {isCancelButton && (
                  <button
                    type="button"
                    className="mt-3 w-full inline-flex justify-center rounded-md border border-gray-300 shadow-sm px-4 py-2 bg-blue-500 text-base font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm"
                    onClick={() => setIsModal(false)}
                  >
                    취소
                  </button>
                )}
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
