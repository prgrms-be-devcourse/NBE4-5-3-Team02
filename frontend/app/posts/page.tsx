"use client";

import { useEffect, useState } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { motion } from "framer-motion";
import { MagnifyingGlassIcon } from "@heroicons/react/24/outline";

interface Availability {
  date: string | null;
  startTime: string;
  endTime: string;
  recurrenceDays: number;
  recurring: boolean;
}

interface Post {
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

export default function PostsPage() {
  const searchParams = useSearchParams();
  const router = useRouter();

  // 검색어 상태값
  const [searchQuery, setSearchQuery] = useState(
    searchParams.get("keyword") || ""
  );

  // 필터 상태값
  const [selectedDistrict, setSelectedDistrict] = useState(
    searchParams.get("district") || ""
  );
  const [distance, setDistance] = useState(searchParams.get("distance") || "1"); // 반경 거리 (1km 기본값)
  const [priceType, setPriceType] = useState(
    searchParams.get("priceType") || ""
  ); // 가격 타입
  const [category, setCategory] = useState(searchParams.get("category") || ""); // 카테고리

  const [latitude, setLatitude] = useState<number | null>(null);
  const [longitude, setLongitude] = useState<number | null>(null);

  // 게시물 상태값
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(false);

  // const [error, setError] = useState("");
  // const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  // const observer = useRef<IntersectionObserver | null>(null);

  // 지역구 목록 (latitude, longitude 포함)
  const districtsWithCoords: {
    [key: string]: { latitude: number; longitude: number };
  } = {
    강남구: { latitude: 37.5172, longitude: 127.0473 },
    서초구: { latitude: 37.4837, longitude: 127.0324 },
    송파구: { latitude: 37.5145, longitude: 127.105 },
    강동구: { latitude: 37.5301, longitude: 127.1238 },
    마포구: { latitude: 37.5665, longitude: 126.9012 },
  };
  const distanceOptions = ["1", "3", "5"]; // 반경 거리 (km)
  const priceTypeOptions = ["HOUR", "DAY"]; // 가격 타입
  const categoryOptions = ["TOOL", "ELECTRONICS"]; // 카테고리

  const BASE_URL = process.env.NEXT_PUBLIC_BASE_URL;

  // 필터 변경 시 URL 업데이트 & 즉시 API 호출
  const updateSearchParams = () => {
    const queryString = new URLSearchParams({
      distance,
      priceType,
      category,
    });

    // latitude, longitude가 설정된 경우만 추가 (district 제거)
    if (latitude !== null && longitude !== null) {
      queryString.append("latitude", latitude.toString());
      queryString.append("longitude", longitude.toString());
    }

    router.push(`/posts?${queryString.toString()}`, { scroll: false });
    fetchPosts(0);
  };

  // 지역 선택 시 위도/경도 설정
  const handleDistrictChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selected = e.target.value;
    setSelectedDistrict(selected);

    if (selected && districtsWithCoords[selected]) {
      setLatitude(districtsWithCoords[selected].latitude);
      setLongitude(districtsWithCoords[selected].longitude);
    } else {
      setLatitude(null);
      setLongitude(null);
    }
  };

  // 이용 가능 날짜 범위 계산 함수
  const getAvailabilityRange = (availabilities: Availability[]): string => {
    if (!availabilities || availabilities.length === 0) {
      return "예약 없음";
    }

    // 날짜를 정렬하여 가장 빠른 날짜와 가장 늦은 날짜 찾기
    const sortedDates = availabilities
      .filter((a) => a.date !== null)
      .map((a) => new Date(a.date as string))
      .sort((a, b) => a.getTime() - b.getTime());

    if (sortedDates.length === 0) return "예약 없음";

    const start = sortedDates[0];
    // const end = sortedDates[sortedDates.length - 1];

    // return `${start.toLocaleDateString()} ~ ${end.toLocaleDateString()}`;
    return `${start.toLocaleDateString()} ~ `;
  };

  useEffect(() => {
    updateSearchParams();
    setPosts([]);
    // setPage(0);
    fetchPosts(0);
  }, [searchQuery, selectedDistrict, distance, priceType, category]); // 카테고리 상태를 useEffect 의존성 배열에 추가

  // 게시물 검색 API 호출
  const fetchPosts = async (pageNumber: number) => {
    if (loading || pageNumber >= totalPages) return;
    setLoading(true);

    try {
      const url = new URL(`${BASE_URL}/api/v1/posts/search`);

      url.searchParams.append("page", pageNumber.toString());
      if (searchQuery) url.searchParams.append("keyword", searchQuery);

      if (latitude !== null && longitude !== null) {
        url.searchParams.append("latitude", latitude.toString());
        url.searchParams.append("longitude", longitude.toString());
      }

      if (distance) url.searchParams.append("distance", distance);
      if (priceType) url.searchParams.append("priceType", priceType);
      if (category) url.searchParams.append("category", category); // 카테고리 파라미터 추가

      console.log(`Fetching posts from: ${url.toString()}`); // API 호출 URL 로깅

      const response = await fetch(url.toString(), {
        method: "GET",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
      });

      if (!response.ok) throw new Error("게시물을 불러오는 데 실패했습니다.");

      const data = await response.json();
      const newPosts: Post[] = data.data.content || [];

      setPosts((prevPosts) => {
        const existingIds = new Set(prevPosts.map((post) => post.id));
        const filteredNewPosts = newPosts.filter(
          (post) => !existingIds.has(post.id)
        );

        return [...prevPosts, ...filteredNewPosts];
      });

      setTotalPages(data.data.totalPages);

    } catch (_error) {
      console.log("error: ",_error);
      // setError(err instanceof Error ? err.message : "알 수 없는 오류 발생");
    } finally {
      setLoading(false);
    }
  };

  // 카테고리 변경 핸들러
  const handleCategoryChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setCategory(e.target.value); // 카테고리 상태 업데이트
  };

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 1.5 }}
      className="min-h-screen bg-gray-100 flex flex-col items-center py-10 px-4"
    >
      <h1 className="text-3xl font-bold text-gray-800 mb-6">
        🔍 검색된 게시물
      </h1>

      {/* 검색창 */}
      <section className="flex justify-center w-full max-w-3xl mb-6">
        <form
          onSubmit={(e) => {
            e.preventDefault();
            updateSearchParams();
          }}
          className="flex items-center bg-white rounded-full px-6 py-4 shadow-xl w-full border-2 border-green-100"
        >
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="검색어 입력"
            className="flex-grow bg-transparent text-lg focus:outline-none placeholder-green-300"
          />
          <button
            type="submit"
            className="ml-4 p-3 bg-green-600 rounded-full hover:bg-green-700 transition-colors"
          >
            <MagnifyingGlassIcon className="w-7 h-7 text-white" />
          </button>
        </form>

        {/* ✅ 게시물 작성 버튼 추가 */}
        <button
          onClick={() => router.push("/posts/create")} // 게시물 작성 페이지로 이동
          className="ml-4 px-4 py-3 bg-blue-600 text-white rounded-full hover:bg-blue-700 transition-colors shadow-md"
        >
          게시물 작성
        </button>
      </section>

      {/* 필터 선택 */}
      <section className="flex flex-wrap justify-center gap-4 mb-6 **bg-white rounded-full px-6 py-4 shadow-xl w-full**">
        {" "}
        {/* ✅ 바깥쪽 테두리 제거 */}
        {/* 지역 선택 */}
        <select
          value={selectedDistrict}
          onChange={handleDistrictChange}
          className="p-2 border rounded"
        >
          <option value="">전체 지역</option>
          {Object.keys(districtsWithCoords).map((district) => (
            <option key={district} value={district}>
              {district}
            </option>
          ))}
        </select>
        {/* 반경 거리 선택 */}
        <select
          value={distance}
          onChange={(e) => setDistance(e.target.value)}
          className="p-2 border rounded-full focus:ring-2 focus:ring-green-500 focus:border-green-500 text-gray-700 shadow-sm bg-white"
        >
          {distanceOptions.map((r) => (
            <option
              key={r}
              value={r}
              className="text-gray-700 hover:bg-gray-100"
            >
              {r}km
            </option>
          ))}
        </select>
        {/* 가격 타입 선택 */}
        <select
          value={priceType}
          onChange={(e) => setPriceType(e.target.value)}
          className="p-2 border rounded-full focus:ring-2 focus:ring-green-500 focus:border-green-500 text-gray-700 shadow-sm bg-white"
        >
          <option value="" className="text-gray-500">
            가격 타입
          </option>
          {priceTypeOptions.map((type) => (
            <option
              key={type}
              value={type}
              className="text-gray-700 hover:bg-gray-100"
            >
              {type}
            </option>
          ))}
        </select>
        {/* 카테고리 선택 */}
        <select
          value={category}
          onChange={handleCategoryChange}
          className="p-2 border rounded-full focus:ring-2 focus:ring-green-500 focus:border-green-500 text-gray-700 shadow-sm bg-white"
        >
          <option value="" className="text-gray-500">
            카테고리
          </option>
          {categoryOptions.map((cat) => (
            <option
              key={cat}
              value={cat}
              className="text-gray-700 hover:bg-gray-100"
            >
              {cat}
            </option>
          ))}
        </select>
      </section>

      {/* 기존 게시물 조회 유지 */}
      <div className="w-full max-w-4xl grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {posts.length > 0 ? (
          posts.map((post) => (
            <motion.div
              key={post.id}
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
              onClick={() => router.push(`/posts/${post.id}`)}
              className="bg-white p-5 rounded-2xl shadow-md hover:shadow-xl cursor-pointer transition-shadow duration-300"
            >
              {/* 이미지 */}
              {post.images.length > 0 ? (
                <img
                  src={post.images[0]}
                  alt={post.title}
                  className="w-full h-40 object-cover rounded-xl mb-4"
                />
              ) : (
                <div className="w-full h-40 bg-gray-200 flex items-center justify-center rounded-xl mb-4">
                  <span className="text-gray-500">이미지 없음</span>
                </div>
              )}

              {/* 제목 */}
              <h2 className="text-lg font-bold text-gray-900">{post.title}</h2>

              {/* 가격 및 카테고리 */}
              <div className="mt-3 flex justify-between items-center text-gray-500 text-sm">
                <span className="bg-blue-100 text-blue-700 px-2 py-1 rounded-md text-xs">
                  {post.category}
                </span>
                <span className="font-semibold text-gray-700">
                  {post.price.toLocaleString()}원/{post.priceType}
                </span>
              </div>

              {/* 조회수 및 등록일 */}
              <div className="mt-3 flex justify-between text-gray-400 text-xs">
                <span>조회수: {post.viewCount}</span>
                <span>
                  등록일:{" "}
                  {post.createdAt
                    ? new Date(post.createdAt).toLocaleDateString()
                    : "등록일 없음"}
                </span>
              </div>

              {/* 이용 가능 날짜 */}
              <div className="mt-2 text-sm text-gray-400 text-xs">
                이용 가능: {getAvailabilityRange(post.availabilities)}
              </div>
            </motion.div>
          ))
        ) : (
          <p className="text-gray-600 col-span-full text-center">
            검색 결과가 없습니다.
          </p>
        )}
      </div>
    </motion.div>
  );

}

