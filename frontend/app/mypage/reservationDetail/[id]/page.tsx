import ClientPage from "./ClientPage";

const mockUpOwner = {
  result: "200-1",
  data: {
    id: 1,
    nickname: "Owner",
    username: "testId",
    profileImage: "image.png",
    email: "test@gmail.com",
    phoneNumber: "000-0000-0000",
    address: {
      mainAddress: "서울시 00구 00동",
      detailAddress: "00아파트 101동 1호",
      zipcode: "12345",
    },
    createdAt: "2025-03-04T12:14:00+09:00",
    score: 80,
    credit: 10000,
  },
};

const mockUpRenter = {
  result: "200-1",
  data: {
    id: 2,
    nickname: "Owner",
    username: "testId",
    profileImage: "image.png",
    email: "test@gmail.com",
    phoneNumber: "000-0000-0000",
    address: {
      mainAddress: "서울시 00구 00동",
      detailAddress: "00아파트 202동 2호",
      zipcode: "12345",
    },
    createdAt: "2025-03-04T12:14:00+09:00",
    score: 80,
    credit: 10000,
  },
};

export default async function Page({
  params,
}: {
  params: {
    id: number;
  };
}) {
  const { id } = await params;

  console.log("api:", `http://localhost:8080/api/v1/reservations/${id}`);
  const getReservationData = await fetch(
    `http://localhost:8080/api/v1/reservations/${id}`,
    {
      method: "GET",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
    }
  );

  const getDepositData = await fetch(
    `http://localhost:8080/api/v1/deposits/rid/${id}`,
    {
      method: "GET",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
    }
  );

  let depositData = null;
  let reservationData = null;

  if (getReservationData.ok) {
    const Data = await getReservationData.json();
    if (Data?.code !== "200-1") {
      console.error(`에러가 발생했습니다. \n${Data?.msg}`);
    }
    reservationData = Data.data;
  } else {
    console.error("Error fetching data:", getReservationData.status);
  }

  if (getDepositData.ok) {
    const Data = await getDepositData.json();
    if (Data?.code !== "200-1") {
      console.error(`에러가 발생했습니다. \n${Data?.msg}`);
    }
    depositData = Data.data;
  } else {
    console.error("Error fetching data:", getDepositData.status);
  }

  return (
    <ClientPage
      reservation={reservationData}
      deposit={depositData}
      me={mockUpRenter.data}
    />
  );
}
