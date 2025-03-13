import ClientPage from "./CllientPage";

export default function Page({
  params,
}: {
  params: {
    id: string;
  };
}) {
  const { id } = params;
  return <ClientPage reservationId={id} />;
}
