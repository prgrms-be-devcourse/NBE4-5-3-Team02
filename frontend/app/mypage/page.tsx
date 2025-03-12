import ClientPage from "./ClientPage";

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

  return <ClientPage />;
}
