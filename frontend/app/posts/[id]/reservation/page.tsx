import ClientPage from "./ClientPage";

export default async function Page({ params }: { params: any }) {
  const postId = parseInt(params.id, 10); // 숫자로 변환 (10진수)

  if (isNaN(postId)) {
    return <div>Invalid post ID</div>;
  }

  return <ClientPage postid={postId} />;
}
