import ClientPage from "./ClientPage";
import { RequestCookie } from "next/dist/compiled/@edge-runtime/cookies";
import { cookies } from "next/headers";

export default async function page() {
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
  //     latitude: 37.456,
  //     longitude: 126.789,
  //     createdAt: "2025-03-04T12:14:00+09:00",
  //     score: 80,
  //     credit: 10000,
  //   },
  // };

  // const me = mockUpUser.data;

  return <ClientPage />;
}
