import ClientPage from "./ClientPage";

// 페이지 디렉토리 이동 필요 예상 -> (app/post/[id]/reservation/)

const mockUpUser = {
  result: "200-1",
  data: {
    id: 2,
    nickname: "닉네임",
    username: "testId",
    profileImage: "image.png",
    email: "test@gmail.com",
    phoneNumber: "000-0000-0000",
    address: {
      mainAddress: "서울시 00구 00동",
      detailAddress: "00아파트 00동 00호",
      zipcode: "12345",
    },
    createdAt: "2025-03-04T12:14:00+09:00",
    score: 80,
    credit: 10000,
  },
};

const mockUpPost = {
  result: "200-1",
  data: {
    id: 1,
    userId: 1,
    title: "전동 드릴 대여합니다",
    content: "전동 드릴 대여합니다. 연락주세요",
    createdAt: "2025-03-10T10:11:00",
    updatedAt: "2025-03-10T10:11:00",
    category: "TOOL",
    priceType: "HOUR",
    price: 10000,
    latitude: 37.123456,
    longitude: 127.123456,
    viewCount: 0,
  },
};
const me = mockUpUser.data;
const post = mockUpPost.data;
export default function Page() {
  return <ClientPage me={me} post={post} />;
}
