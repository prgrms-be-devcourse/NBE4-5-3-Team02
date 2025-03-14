import ClientPage from "./ClientPage";

// 페이지 디렉토리 이동 필요 예상 -> (app/post/[id]/reservation/)

export default async function Page({
  params,
}: {
  params: {
    id: number;
  };
}) {
  const { id } = await params;

  return <ClientPage postid={id} />;
}
