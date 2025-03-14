import ClientPage from "./ClientPage";

// 페이지 디렉토리 이동 필요 예상 -> (app/post/[id]/reservation/)

export default function Page() {
  return <ClientPage />;
}

// 페이지 디렉토리 이동 후 예상 코드
// export default async function Page({
//   params,
// }: {
//   params: {
//     id: number;
//   };
// }) {
//   const { id } = await params;

//   return <ClientPage id={id} />;
// }
