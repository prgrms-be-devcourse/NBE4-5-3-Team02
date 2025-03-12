// app/reservations/complete/page.jsx
"use client";

import { useRouter } from "next/navigation";
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
  renter,
}: {
  reservation: Reservation;
  deposit: Deposit;
  me: me;
  renter: me;
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
        const response = await fetch(
          `http://localhost:8080/api/v1/reservations/${reservation.id}/approve`,
          {
            method: "PATCH",
          }
        );
        if (response.ok) {
          window.location.reload();
        } else {
          const errorData = await response.json();
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
        const response = await fetch(
          `http://localhost:8080/api/v1/reservations/${
            reservation.id
          }/reject?reason=${encodeURIComponent(rejectionReason)}`,
          {
            method: "PATCH",
          }
        );
        if (response.ok) {
          setModalMessage("거절이 완료되었습니다.");
          setShowModal(true);
        } else {
          const errorData = await response.json();
          alert(`거절 실패: ${errorData.message || "서버 오류"}`);
        }
      } catch (error) {
        alert("거절 처리 중 오류 발생");
        console.error(error);
      }
    } else if (actionType === "cancel") {
      // 취소 로직
      try {
        const response = await fetch(
          `http://localhost:8080/api/v1/reservations/${reservation.id}/cancel`,
          {
            method: "PATCH",
          }
        );
        if (response.ok) {
          window.location.reload(); // 성공 시 새로고침
        } else {
          const errorData = await response.json();
          alert(`취소 실패: ${errorData.message || "서버 오류"}`);
        }
      } catch (error) {
        alert("취소 처리 중 오류 발생");
        console.error(error);
      }
    }
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
      {/* 확인 모달 */}
      {showConfirmModal && (
        <div className="fixed inset-0 flex justify-center items-center bg-transparent backdrop-filter backdrop-blur-lg">
          <div className="bg-white p-4 rounded-lg">
            <p>{modalMessage}</p>
            {actionType === "reject" && (
              <div>
                <input
                  type="text"
                  value={rejectionReason}
                  onChange={(e) => setRejectionReason(e.target.value)}
                  placeholder="거절 사유를 입력하세요"
                  className="border p-2 rounded w-full my-2"
                />
              </div>
            )}
            <button
              className="mt-4 bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded w-[50%]"
              onClick={confirmAction}
            >
              예
            </button>
            <button
              className="mt-4 bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded w-[50%]"
              onClick={() => setShowConfirmModal(false)}
            >
              아니오
            </button>
          </div>
        </div>
      )}
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
  me,
  owner,
}: {
  reservation: Reservation;
  deposit: Deposit;
  me: me;
  owner: me;
}) {
  const [showModal, setShowModal] = useState<boolean>(false);

  if (!owner) {
    return <div>Loading...</div>; // 또는 return null;
  }

  const openModal = () => setShowModal(true);
  const closeModal = () => setShowModal(false);
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
      {me.id === reservation.renterId ? (
        <button
          className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
          onClick={openModal}
        >
          소유자 {owner.nickname}님의 상세 정보 보기
        </button>
      ) : null}
      {showModal && owner && (
        <div className="fixed inset-0 flex justify-center items-center text-center bg-transparent backdrop-filter backdrop-blur-lg">
          <div className="relative p-8 bg-white w-1/2 rounded-lg">
            <h2 className="text-2xl font-bold mb-4">
              {owner.nickname}님의 정보
            </h2>
            <p>이메일: {owner.email}</p>
            <p>
              <b>{owner.nickname}</b> 님의 점수는 {owner.score}점입니다!
            </p>
            <br />
            <p className="font-bold">상세 주소</p>
            <p>{owner.address.mainAddress}</p>
            <p>{owner.address.detailAddress}</p>
            <br />
            <p className="font-bold">전화번호</p>
            <p>{owner.phoneNumber}</p>
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

function InProgressStatus({
  reservation,
  deposit,
}: {
  reservation: Reservation;
  deposit: Deposit;
}) {
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [modalMessage, setModalMessage] = useState("");
  const [selectedIssue, setSelectedIssue] = useState<{
    value: string;
    label: string;
    issueType: "owner" | "renter";
  } | null>(null); // 선택된 이슈

  // 선택 가능한 사유 목록 (issueType 추가)
  const issueOptions = [
    { value: "DAMAGE_REPORTED", label: "물건 훼손", issueType: "renter" },
    { value: "ITEM_LOSS", label: "물건 분실", issueType: "renter" },
    {
      value: "UNRESPONSIVE_RENTER",
      label: "대여자의 무응답",
      issueType: "owner",
    }, // owner
  ];

  const handleIssue = () => {
    setModalMessage("문제가 발생했습니까?");
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
      apiUrl = `http://localhost:8080/api/v1/reservations/${
        reservation.id
      }/ownerIssue?reason=${encodeURIComponent(selectedIssue.value)}`;
    } else if (selectedIssue.issueType === "renter") {
      apiUrl = `http://localhost:8080/api/v1/reservations/${
        reservation.id
      }/renterIssue?reason=${encodeURIComponent(selectedIssue.value)}`;
    }

    try {
      const response = await fetch(apiUrl, {
        method: "PATCH",
      });

      if (response.ok) {
        window.location.reload();
      } else {
        const errorData = await response.json();
        alert(`문제 해결 요청 실패: ${errorData.message || "서버 오류"}`);
      }
    } catch (error) {
      alert("문제 해결 요청 중 오류 발생");
      console.error(error);
    }
  };

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
      <p className="text-lg mb-2 font-bold">대여 중 문제가 발생하셨나요?</p>
      <button
        className="mt-4 bg-gray-300 hover:bg-gray-400 text-gray-800 font-bold py-2 px-4 rounded"
        onClick={handleIssue}
      >
        문제 보고하기
      </button>

      {/* 확인 모달 */}
      {showConfirmModal && (
        <div className="fixed inset-0 flex justify-center items-center bg-transparent backdrop-filter backdrop-blur-lg">
          <div className="bg-white w-[50%] p-4 rounded-lg">
            <p>{modalMessage}</p>
            {/* 드롭다운 */}
            <div>
              <select
                value={selectedIssue ? selectedIssue.value : ""} // value 수정
                onChange={(e) => {
                  const selectedValue = e.target.value;
                  const option = issueOptions.find(
                    (opt) => opt.value === selectedValue
                  ); // find로 option 찾기
                  setSelectedIssue(option || null); // 찾은 option으로 selectedIssue 설정
                }}
                className="border p-2 rounded w-full my-2"
              >
                <option value="">사유 선택</option>
                {issueOptions.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
            <div className="flex justify-center">
              <button
                className="mt-4 bg-blue-500 hover:bg-blue-700 text-white mr-10 font-bold py-2 px-4 rounded w-[30%]"
                onClick={confirmAction}
              >
                예
              </button>
              <button
                className="mt-4 bg-gray-500 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded w-[30%]"
                onClick={() => setShowConfirmModal(false)}
              >
                아니오
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
  return (
    <div className="flex flex-col items-center justify-center w-[70%] h-150 border rounded-lg shadow-md p-6 bg-red-200">
      <p className="text-5xl font-bold text-red-600 mb-4">❌</p>
      {me.id === reservation.ownerId && renter ? (
        // 호스트인 경우 대여자 이름 표시

        <p className="text-4xl mb-2 font-bold">
          <span className="text-blue-600 cursor-pointer" onClick={openModal}>
            {renter.nickname}
          </span>
          님의 예약을 취소하셨습니다.
        </p>
      ) : me.id === reservation.renterId ? (
        // 세입자인 경우 예약 확인 메시지 표시
        <>
          <p className="text-4xl font-bold text-green-600 mb-4">
            예약이 취소되었습니다
          </p>
          <p className="text-lg mb-2">다른 예약을 시도해주세요.</p>
        </>
      ) : null}

      <p className="text-lg mb-2 font-bold">사유</p>
      <p className="text-lg mb-2 font-bold">{reservation.rejectionReason}</p>
      <p className="text-lg mb-2">
        {formatDate(reservation.startTime)} ~ {formatDate(reservation.endTime)}
      </p>
      <p className="text-lg mb-8">
        대여료 {reservation.amount - deposit.amount}₩ + 보증금 {deposit.amount}₩
      </p>
      <p className="text-lg mb-2 font-bold">합계 {reservation.amount}₩</p>
      {me.id === reservation.renterId ? (
        <button className="text-lg bg-red-500 text-white px-4 py-2 rounded-lg">
          다시 예약하기
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

function FailedStatus({
  reservation,
  deposit,
  me,
  renter,
  owner,
  reason,
}: {
  reservation: Reservation;
  deposit: Deposit;
  me: me;
  renter: me | null;
  owner: me | null;
  reason: string; // 실패 사유 (API에서 전달받음)
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
    <div className="flex flex-col items-center justify-center w-[70%] h-150 border rounded-lg shadow-md p-6 bg-red-100">
      <p className="text-5xl font-bold text-red-600 mb-4">⚠️</p>
      <p className="text-4xl font-bold text-red-600 mb-4">
        {/* 예약 실패 사유에 따라 다른 메시지 표시 */}
        {reservation.status === "FAILED_OWNER_ISSUE"
          ? "소유자 문제로 예약이 실패했습니다."
          : reservation.status === "FAILED_RENTER_ISSUE"
          ? "대여자 문제로 예약이 실패했습니다."
          : "예약 실패"}
      </p>
      <p className="text-lg mb-2 font-bold">
        사유: {reasonLabel || deposit.returnReason}
      </p>

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

function DoneStatus({
  reservation,
  deposit,
}: {
  reservation: Reservation;
  deposit: Deposit;
}) {
  const router = useRouter(); // useRouter 훅 사용

  const goToReviewPage = () => {
    // 임시 URL.  나중에 실제 리뷰 페이지 URL로 변경해야 함.
    router.push(`/review/${reservation.id}`);
  };
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
      <button
        className="mt-4 bg-green-500 hover:bg-yellow-600 text-white font-bold py-2 px-4 rounded"
        onClick={goToReviewPage}
      >
        유저 리뷰하기
      </button>
    </div>
  );
}

function CancelledStatus({ reservation }: { reservation: Reservation }) {
  return (
    <div className="flex flex-col items-center justify-center w-[70%] h-150 border rounded-lg shadow-md p-6 bg-gray-200">
      <p className="text-5xl font-bold text-gray-600 mb-4">❌</p>
      <p className="text-4xl font-bold text-gray-600 mb-4">
        이미 취소된 예약입니다
      </p>
      <p className="text-lg mb-2">예약 ID: {reservation.id}</p>
    </div>
  );
}

export default function ClientPage({
  reservation,
  deposit,
  me,
}: {
  reservation: Reservation;
  deposit: Deposit;
  me: me;
}) {
  const [renter, setRenter] = useState<me | null>(null);
  const [owner, setOwner] = useState<me | null>(null);

  useEffect(() => {
    console.log("useEffect");
    fetchRenterInfo(reservation.renterId);
    fetchOwnerInfo(reservation.ownerId);
  }, []);

  const fetchRenterInfo = async (renterId: number) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/users/${renterId}`
      );
      if (response.ok) {
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
      const response = await fetch(
        `http://localhost:8080/api/v1/users/${ownerId}`
      );
      if (response.ok) {
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

  const router = useRouter();

  const goToMyPage = () => {
    router.push("/mypage"); // /mypage 경로로 이동
  };

  return (
    <div className="flex flex-col justify-center items-center min-h-screen">
      {reservation.status === "REQUESTED" && (
        <RequestedStatus
          reservation={reservation}
          deposit={deposit}
          me={me}
          renter={renter}
        />
      )}
      {reservation.status === "APPROVED" && (
        <ApprovedStatus
          reservation={reservation}
          deposit={deposit}
          me={me}
          owner={owner}
        />
      )}
      {reservation.status === "IN_PROGRESS" && (
        <InProgressStatus reservation={reservation} deposit={deposit} />
      )}
      {reservation.status === "REJECTED" && (
        <RejectedStatus
          reservation={reservation}
          deposit={deposit}
          me={me}
          renter={renter}
        />
      )}
      {reservation.status === "DONE" && (
        <DoneStatus reservation={reservation} deposit={deposit} />
      )}
      {reservation.status === "FAILED_OWNER_ISSUE" && (
        <FailedStatus
          reservation={reservation}
          deposit={deposit}
          me={me}
          renter={renter}
          owner={owner}
          reason="OWNER_ISSUE"
        />
      )}
      {reservation.status === "FAILED_RENTER_ISSUE" && (
        <FailedStatus
          reservation={reservation}
          deposit={deposit}
          me={me}
          renter={renter}
          owner={owner}
          reason="RENTER_ISSUE"
        />
      )}
      {reservation.status === "CANCELED" && (
        <CancelledStatus reservation={reservation} />
      )}
      <button
        className="mt-4 bg-green-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
        onClick={goToMyPage} // 버튼 클릭 시 goToMyPage 함수 호출
      >
        마이페이지로 이동
      </button>
    </div>
  );
}
