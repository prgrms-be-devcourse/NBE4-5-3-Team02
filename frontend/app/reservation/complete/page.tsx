"use client";

import { useSearchParams } from "next/navigation";
import ClientPage from "./ClientPage";
import { useEffect, useState } from "react";

export default function Page() {
  const searchParams = useSearchParams();
  const [reservation, setReservation] = useState(null);
  const [deposit, setDeposit] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchReservationData = async (reservationId: string) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/reservations/${reservationId}`,
        {
          method: "GET",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      setReservation(data);
      setLoading(false);
    } catch (error: any) {
      setError(error);
      setLoading(false);
    }
  };

  const fetchDepositData = async (reservationId: string) => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/v1/deposits/rid/${reservationId}`,
        {
          method: "GET",
          credentials: "include",
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log("Deposit Data:", data);
      setDeposit(data);
    } catch (error: any) {
      console.error("API 요청 중 오류 발생:", error);
      throw error;
    }
  };

  useEffect(() => {
    const reservationId = searchParams.get("reservationId");
    if (reservationId) {
      fetchReservationData(reservationId);
      fetchDepositData(reservationId);
    }
  }, [searchParams]);

  if (loading) {
    return <div>로딩 중...</div>;
  }

  if (error) {
    return <div>오류 발생: {error.message}</div>;
  }

  if (!reservation) {
    return <div>예약 정보가 없습니다.</div>;
  }

  if (!deposit) {
    return <div>보증금 정보가 없습니다.</div>;
  }

  console.log("reservation", reservation);

  return <ClientPage reservation={reservation.data} deposit={deposit.data} />;
}
