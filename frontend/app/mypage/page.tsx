import ClientPage from "./ClientPage";
import { RequestCookie } from "next/dist/compiled/@edge-runtime/cookies";
import { cookies } from "next/headers";

export default async function page() {
  //TODO: 로그인 개발 완료 후 주석 풀기
  // const myCookie = await cookies();
  // const { isLogin, payload } = parseAccessToken(myCookie.get("token"));

  // if (!isLogin) {
  //   return <div>로그인 필요</div>;
  // }

  // const myParsedData = {
  //   nickname: payload.nickname,
  // };

  // console.log("쿠키에서 가져온 닉네임: ", myParsedData.nickname);

  const getMyInfo = await fetch("http://localhost:8080/api/v1/mypage/me", {
    method: "GET",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
  });

  const getMyReservations = await fetch("http://localhost:8080/api/v1/mypage/reservations", {
    method: "GET",
    credentials: "include", 
    headers: {
      "Content-Type": "application/json",
    },
  });

  let userData = null;
  let reservationData = null;

  if (getMyInfo.ok) {
    const Data = await getMyInfo.json();
    if (Data?.code !== "200-1") {
      console.error(`에러가 발생했습니다. \n${Data?.msg}`);
    }
    userData = Data.data;
  } else {
    console.error("Error fetching data:", getMyInfo.status);
  }

  if (getMyReservations.ok) {
    const Data = await getMyReservations.json();
    if (Data?.code !== "200-1") {
      console.error(`에러가 발생했습니다. \n${Data?.msg}`);
    }
    reservationData = Data.data;
  } else {
    console.error("Error fetching data:", getMyReservations.status);
  }

  // const mockUpUser = {
  //   result: "200-1",
  //   data: {
  //     id: 123,
  //     nickname: "닉네임",
  //     username: "testId",
  //     profileImage: "image.png",
  //     email: "test@gmail.com",
  //     phoneNumber: "000-0000-0000",
  //     address: {
  //       mainAddress: "서울시 00구 00동",
  //       detailAddress: "00아파트 00동 00호",
  //       zipcode: "12345",
  //     },
  //     createdAt: "2025-03-04T12:14:00+09:00",
  //     score: 80,
  //     credit: 10000,
  //   },
  // };

  // const mockUpReservation = {
  //   result: "200-1",
  //   data: {
  //     rentals: [
  //       {
  //         id: 1, //reservation id
  //         title: "전동 드릴",
  //         image: "image.png",
  //         amount: 10000,
  //         startTime: "2025-03-10T10:11:00",
  //         endTime: "2025-03-12T18:12:00",
  //         status: "APPROVED",
  //         isReviewed: false,
  //       },
  //       {
  //         id: 3, //reservation id
  //         title: "망치",
  //         image: "image.png",
  //         amount: 5000,
  //         startTime: "2025-03-11T11:11:00",
  //         endTime: "2025-03-14T18:12:00",
  //         status: "APPROVED",
  //         isReviewed: false,
  //       },
  //     ],
  //     borrows: [
  //       {
  //         id: 2,
  //         title: "공구 세트",
  //         image: "image.png",
  //         amount: 20000,
  //         startTime: "2025-03-05T09:10:00",
  //         endTime: "2025-03-07T15:17:00",
  //         status: "IN_PROGRESS",
  //         isReviewed: false,
  //       },
  //     ],
  //   },
  // };

  // const me = mockUpUser.data;
  const me = userData;
  const reservations = reservationData;

  return <ClientPage me={me} reservations={reservations} />;
}

function parseAccessToken(accessToken: RequestCookie | undefined) {
  let isExpired = true;
  let payload = null;

  if (accessToken) {
    try {
      const tokenParts = accessToken.value.split(".");
      payload = JSON.parse(Buffer.from(tokenParts[1], "base64").toString());
      const expTimestamp = payload.exp * 1000;
      isExpired = Date.now() > expTimestamp;
    } catch (e) {
      console.error("토큰 파싱 중 오류 발생:", e);
    }
  }
  let isLogin = payload !== null;

  return { isLogin, isExpired, payload };
}
