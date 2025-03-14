// app/mypage/reservationDetail/[id]/page.tsx
import ClientPage from "./ClientPage";

export default async function Page({ params }: { params: any }) {
  const reservationId = parseInt(params.id, 10);

  if (isNaN(reservationId)) {
    return <div>Invalid reservation ID</div>;
  }

  // ClientPage에 reservationId만 전달
  return <ClientPage reservationId={reservationId} />;
}
