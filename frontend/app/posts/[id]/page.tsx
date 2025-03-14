"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { motion } from "framer-motion";

import { ArrowLeftIcon } from "@heroicons/react/24/outline";

interface Availability {
  date: string | null;
  startTime: string;
  endTime: string;
  recurrenceDays: number;
  recurring: boolean;
}

interface PostDetail {
  id: number;
  title: string;
  content: string;
  category: string;
  priceType: string;
  price: number;
  latitude: number;
  longitude: number;
  createdAt: string | null;
  viewCount: number;
  images: string[];
  availabilities: Availability[];
}

export default function PostDetailPage() {
  const { id } = useParams();
  const router = useRouter();
  const [post, setPost] = useState<PostDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchPostDetail = async () => {
      try {
        const response = await fetch(
          `http://localhost:8080/api/v1/posts/${id}`,
          {
            method: "GET",
            credentials: "include",
            headers: { "Content-Type": "application/json" },
          }
        );

        if (!response.ok) throw new Error("게시물을 불러오는 데 실패했습니다.");

        const data = await response.json();
        setPost(data.data);
      } catch (err) {
        setError(err instanceof Error ? err.message : "알 수 없는 오류 발생");
      } finally {
        setLoading(false);
      }
    };

    fetchPostDetail();
  }, [id]);

  // 반복되지 않는 예약 날짜 정리
  const nonRecurringDates =
    post?.availabilities.filter((a) => !a.recurring) || [];

  // 반복되는 예약 날짜 정리
  const recurringDates = post?.availabilities.filter((a) => a.recurring) || [];

  // 요일 변환 함수 (1=월요일, 7=일요일)
  const getDayOfWeek = (dayNumber: number) => {
    const days = [
      "월요일",
      "화요일",
      "수요일",
      "목요일",
      "금요일",
      "토요일",
      "일요일",
    ];
    return days[dayNumber - 1] || "알 수 없음"; // 1~7 외의 값이 들어오면 기본값
  };

  if (loading)
    return (
      <p className="text-gray-600 text-center py-10">게시물을 불러오는 중...</p>
    );
    
  if (error) return <p className="text-red-600 text-center py-10">{error}</p>;

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1 }}
      className="min-h-screen bg-gray-100 flex flex-col items-center py-10 px-4"
    >
      {/* 🔙 뒤로 가기 버튼 */}
      {/* <button onClick={() => router.back()} className="absolute top-6 left-6 flex items-center text-gray-600 hover:text-gray-800">
        <ArrowLeftIcon className="w-6 h-6 mr-2" /> 뒤로가기
      </button> */}

      <h1 className="text-3xl font-bold text-gray-800 mb-6">{post?.title}</h1>

      {/* 이미지 슬라이드 */}
      <div className="w-full max-w-3xl mb-6">
        {post?.images.length ? (
          <img
            src={post.images[0]}
            alt={post.title}
            className="w-full h-72 object-cover rounded-xl shadow-md"
          />
        ) : (
          <div className="w-full h-72 bg-gray-300 flex items-center justify-center rounded-xl">
            <span className="text-gray-600">이미지 없음</span>
          </div>
        )}
      </div>

      {/* 게시물 상세 정보 */}
      <div className="bg-white p-6 rounded-2xl shadow-lg max-w-3xl w-full">
        <p className="text-lg text-gray-700 mb-4">{post?.content}</p>

        <div className="grid grid-cols-2 gap-4 text-gray-600">
          <p>
            <strong className="text-gray-800">품목 종류:</strong>{" "}
            {post?.category}
          </p>
          <p>
            <strong className="text-gray-800">가격:</strong>{" "}
            {post?.price.toLocaleString()}원 / {post?.priceType}
          </p>
          <p>
            <strong className="text-gray-800">조회수:</strong> {post?.viewCount}
          </p>
          <p>
            <strong className="text-gray-800">등록일:</strong>{" "}
            {post?.createdAt
              ? new Date(post.createdAt).toLocaleDateString()
              : "등록일 없음"}
          </p>
        </div>

        {/* 이용 가능 날짜 테이블 */}
        <div className="mt-6">
          <h2 className="text-xl font-semibold text-gray-800 mb-4">
            📅 이용 가능 일정
          </h2>
          <div className="border border-gray-300 rounded-lg overflow-hidden">
            <table className="w-full border-collapse">
              <thead className="bg-gray-200">
                <tr>
                  <th className="p-3 border">날짜</th>
                  <th className="p-3 border">시간</th>
                </tr>
              </thead>
              <tbody>
                {/* 반복되지 않는 날짜 표시 */}
                {nonRecurringDates.map((a, index) => (
                  <tr
                    key={`nonrecurring-${index}`}
                    className="text-center border-t"
                  >
                    <td className="p-3 border">
                      {a.date ? new Date(a.date).toLocaleDateString() : "미정"}
                    </td>
                    <td className="p-3 border">
                      {a.startTime.split(" ")[1]} ~ {a.endTime.split(" ")[1]}
                    </td>
                  </tr>
                ))}

                {/* 반복되는 날짜 표시 */}
                {recurringDates.map((a, index) => (
                  <tr
                    key={`recurring-${index}`}
                    className="text-center border-t bg-yellow-100"
                  >
                    <td className="p-3 border">
                      매주 {getDayOfWeek(a.recurrenceDays)} 가능
                    </td>
                    <td className="p-3 border">
                      {a.startTime.split(" ")[1]} ~ {a.endTime.split(" ")[1]}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
        {/* 예약 및 수정 버튼 */}
        <div className="mt-6 flex justify-between">
          {/* 수정하기 버튼 (현재는 동작 없음) */}
          <button className="bg-gray-500 text-white py-2 px-6 rounded-lg hover:bg-gray-600 transition">
            수정하기
          </button>

          {/* 예약하기 버튼 (예약 페이지로 이동) */}
          <button
            className="bg-blue-600 text-white py-2 px-6 rounded-lg hover:bg-blue-700 transition"
            onClick={() => router.push("/reservation")}
          >
            예약하기
          </button>
        </div>
      </div>
    </motion.div>
  );
}
