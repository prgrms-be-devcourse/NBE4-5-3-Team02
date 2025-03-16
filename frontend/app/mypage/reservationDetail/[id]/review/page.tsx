import ClientPage from "./ClientPage";

export default function Page({ params }: { params: any }) {
  const { id } = params;
  return <ClientPage reservationId={id} />;
}
