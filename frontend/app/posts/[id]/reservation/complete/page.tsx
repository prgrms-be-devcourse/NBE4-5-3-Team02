"use client";

import { useSearchParams } from "next/navigation";
import ClientPage from "./ClientPage";
import { useEffect, useState } from "react";
import { fetchWithAuth } from "@/app/lib/util/fetchWithAuth";

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

interface post {
  id: number;
  userId: number;
  title: string;
  priceType: string;
  price: number;
}

export default function Page() {
  const searchParams = useSearchParams();
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

  const BASE_URL = "http://localhost:8080";

  const getReservation = async (reservationId: string) => {
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

    if (getReservationInfo.ok) {
      const Data = await getReservationInfo.json();
      if (Data?.code !== "200-1") {
        console.error(`에러가 발생했습니다. \n${Data?.msg}`);
      }
      setReservation(Data?.data);
      console.log("deposit : ", Data?.data);
    } else {
      console.error("Error fetching data:", getReservationInfo.status);
    }
  };

  const getDeposit = async (reservationId: string) => {
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

    if (getDepositInfo.ok) {
      const Data = await getDepositInfo.json();
      if (Data?.code !== "200-1") {
        console.error(`에러가 발생했습니다. \n${Data?.msg}`);
      }
      setDeposit(Data?.data);
      console.log("deposit : ", Data?.data);
    } else {
      console.error("Error fetching data:", getDepositInfo.status);
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

    if (getPostInfo.ok) {
      const Data = await getPostInfo.json();
      if (Data?.code !== "200-1") {
        console.error(`에러가 발생했습니다. \n${Data?.msg}`);
      }
      setPost(Data?.data);
      console.log("data : ", Data?.data);
    } else {
      console.error("Error fetching data:", getPostInfo.status);
    }
  };

  useEffect(() => {
    const reservationId = searchParams.get("reservationId");
    if (reservationId) {
      getReservation(reservationId);
      getDeposit(reservationId);
    }
  }, [searchParams]);

  useEffect(() => {
    if (reservation && reservation.postId) {
      getPost(reservation.postId);
    }
  }, [reservation]);

  console.log("reservation", reservation);
  console.log("Post", post);
  console.log("Deposit", deposit);

  return <ClientPage reservation={reservation} deposit={deposit} post={post} />;
}
