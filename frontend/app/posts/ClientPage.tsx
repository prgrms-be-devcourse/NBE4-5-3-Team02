'use client';

import { useEffect, useState } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { motion } from 'framer-motion';
import {
    MapPinIcon,
    MagnifyingGlassIcon,
} from "@heroicons/react/24/outline";
import {fetchWithAuth} from "@/app/lib/util/fetchWithAuth";
import {ChevronLeftIcon, ChevronRightIcon, ShoppingBagIcon} from "lucide-react";

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
    const [searchQuery, setSearchQuery] = useState(searchParams.get('keyword') || '');

    // 필터 상태값
    const [selectedDistrict, setSelectedDistrict] = useState(searchParams.get('district') || '');
    const [distance, setDistance] = useState(searchParams.get('distance') || '1'); // 반경 거리 (1km 기본값)
    const [priceType, setPriceType] = useState(searchParams.get('priceType') || ''); // 가격 타입
    const [category, setCategory] = useState(searchParams.get('category') || ''); // 카테고리

    const [latitude, setLatitude] = useState<number | null>(null);
    const [longitude, setLongitude] = useState<number | null>(null);

    // 게시물 상태값
    const [posts, setPosts] = useState<Post[]>([]);
    const [loading, setLoading] = useState(false);
    // const [ setError] = useState('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);

    // 지역구 목록 (latitude, longitude 포함)
    const districtsWithCoords: { [key: string]: { latitude: number; longitude: number } } = {
        "강남구": {latitude: 37.5172, longitude: 127.0473},
        "서초구": {latitude: 37.4837, longitude: 127.0324},
        "송파구": {latitude: 37.5145, longitude: 127.1050},
        "강동구": {latitude: 37.5301, longitude: 127.1238},
        "마포구": {latitude: 37.5665, longitude: 126.9012}
    };
    const distanceOptions = ['1', '3', '5']; // 반경 거리 (km)
    const priceTypeOptions = ['HOUR', 'DAY']; // 가격 타입
    const categoryOptions = ['TOOL', 'ELECTRONICS']; // 카테고리

    // 필터 변경 시 URL 업데이트 & 즉시 API 호출
    const updateSearchParams = () => {
        const queryString = new URLSearchParams({
            distance,
            priceType,
            category
        });

        // latitude, longitude가 설정된 경우만 추가 (district 제거)
        if (latitude !== null && longitude !== null) {
            queryString.append('latitude', latitude.toString());
            queryString.append('longitude', longitude.toString());
        }

        router.push(`/posts?${queryString.toString()}`, {scroll: false});
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
            return '예약 없음';
        }

        // 날짜를 정렬하여 가장 빠른 날짜와 가장 늦은 날짜 찾기
        const sortedDates = availabilities
            .filter((a) => a.date !== null)
            .map((a) => new Date(a.date as string))
            .sort((a, b) => a.getTime() - b.getTime());

        if (sortedDates.length === 0) return '예약 없음';

        const start = sortedDates[0];
        // const end = sortedDates[sortedDates.length - 1];

        // return `${start.toLocaleDateString()} ~ ${end.toLocaleDateString()}`;
        return `${start.toLocaleDateString()} ~ `;
    };

    useEffect(() => {
        updateSearchParams();
        setPosts([]);
        setPage(0);
        fetchPosts(0);
    }, [searchQuery, selectedDistrict, distance, priceType, category]); // 카테고리 상태를 useEffect 의존성 배열에 추가

    // 게시물 검색 API 호출
    const fetchPosts = async (pageNumber: number) => {
        if (loading || pageNumber >= totalPages) return;
        setLoading(true);

        try {
            const url = new URL('http://localhost:8080/api/v1/posts/search');
            url.searchParams.append('page', pageNumber.toString());
            if (searchQuery) url.searchParams.append('keyword', searchQuery);

            if (latitude !== null && longitude !== null) {
                url.searchParams.append('latitude', latitude.toString());
                url.searchParams.append('longitude', longitude.toString());
            }

            if (distance) url.searchParams.append('distance', distance);
            if (priceType) url.searchParams.append('priceType', priceType);
            if (category) url.searchParams.append('category', category); // 카테고리 파라미터 추가

            console.log(`Fetching posts from: ${url.toString()}`); // API 호출 URL 로깅

            const response = await fetchWithAuth(url.toString(), {
                method: 'GET',
                credentials: 'include',
                headers: {'Content-Type': 'application/json'},
            });

            if (!response) throw new Error('No response from server');
            const data = await response.json();

            if (!response?.ok) throw new Error('게시물을 불러오는 데 실패했습니다.');
            const newPosts: Post[] = data.data.content || [];

            setPosts((prevPosts) => {
                const existingIds = new Set(prevPosts.map((post) => post.id));
                const filteredNewPosts = newPosts.filter((post) => !existingIds.has(post.id));
                return [...prevPosts, ...filteredNewPosts];
            });

            setTotalPages(data.data.totalPages);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    // 카테고리 변경 핸들러
    const handleCategoryChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setCategory(e.target.value); // 카테고리 상태 업데이트
    };

    // 페이징 처리

    const handleNextPage = () => {
        if (page < totalPages - 1) {
            setPage(page + 1);
            fetchPosts(page + 1); // 페이지 증가 후 API 호출
        }
    };

    const handlePreviousPage = () => {
        if (page > 0) {
            setPage(page - 1);
            fetchPosts(page - 1); // 페이지 감소 후 API 호출
        }
    };


    return (
        <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.5 }}
            className="w-full"
        >

            {/* 검색 헤더 섹션 */}
            <div className="max-w-6xl mx-auto pt-10">
                <motion.form
                    onSubmit={(e) => {
                        e.preventDefault();
                        updateSearchParams();
                    }}
                    className="bg-white rounded-2xl p-6 shadow-xl border-2 border-green-100 mb-12"
                >
                    <div className="flex flex-col md:flex-row gap-6">
                        {/* 검색창 */}
                        <div className="flex-1 relative">
                            <div className="flex items-center bg-gray-50 rounded-xl px-6 py-4">
                                <MagnifyingGlassIcon className="w-6 h-6 text-green-600 mr-4"/>
                                <input
                                    type="text"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    placeholder="물품명 또는 키워드 검색"
                                    className="flex-grow bg-transparent text-lg focus:outline-none placeholder-green-300"
                                />
                            </div>
                        </div>

                        {/* 필터 그룹 */}
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 w-full md:w-auto">
                            {/* 지역 선택 */}
                            <select
                                value={selectedDistrict}
                                onChange={handleDistrictChange}
                                className="p-3 bg-gray-50 rounded-xl border-2 border-green-100 text-gray-700 focus:ring-2 focus:ring-green-500"
                            >
                                <option value="">전체 지역</option>
                                {Object.keys(districtsWithCoords).map((district) => (
                                    <option key={district} value={district}>
                                        {district}
                                    </option>
                                ))}
                            </select>

                            {/* 거리 선택 */}
                            <select
                                value={distance}
                                onChange={(e) => setDistance(e.target.value)}
                                className="p-3 bg-gray-50 rounded-xl border-2 border-green-100 text-gray-700 focus:ring-2 focus:ring-green-500"
                            >
                                {distanceOptions.map((r) => (
                                    <option key={r} value={r}>
                                        {r}km 이내
                                    </option>
                                ))}
                            </select>

                            {/* 가격 타입 */}
                            <select
                                value={priceType}
                                onChange={(e) => setPriceType(e.target.value)}
                                className="p-3 bg-gray-50 rounded-xl border-2 border-green-100 text-gray-700 focus:ring-2 focus:ring-green-500"
                            >
                                <option value="">가격 타입</option>
                                {priceTypeOptions.map((type) => (
                                    <option key={type} value={type}>
                                        {type === 'HOUR' ? '시간당' : '일당'}
                                    </option>
                                ))}
                            </select>

                            {/* 카테고리 */}
                            <select
                                value={category}
                                onChange={handleCategoryChange}
                                className="p-3 bg-gray-50 rounded-xl border-2 border-green-100 text-gray-700 focus:ring-2 focus:ring-green-500"
                            >
                                <option value="">전체 카테고리</option>
                                {categoryOptions.map((cat) => (
                                    <option key={cat} value={cat}>
                                        {cat === 'TOOL' ? '공구' : '전자기기'}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>
                </motion.form>

                {/* 플로팅 액션 버튼으로 변경 */}
                <div className="fixed bottom-6 right-6 z-50 animate-bounce-once">
                    <motion.button
                        onClick={() => router.push("/posts/create")}
                        className="px-6 py-4 bg-gradient-to-r from-green-500 to-green-600 text-white
              rounded-xl shadow-2xl hover:shadow-3xl font-semibold
              flex items-center gap-3 text-lg group relative
              ring-2 ring-white/20 hover:ring-green-200 transition-all"
                        whileHover={{ y: -2, scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                    >
                        {/* 호버 시 확대되는 아이콘 애니메이션 */}
                        <motion.div whileHover={{ rotate: 180 }}>
                            <ShoppingBagIcon className="w-7 h-7 text-green-100 group-hover:text-white" />
                        </motion.div>

                        <span className="tracking-wide">물품 등록</span>

                        {/* 미세한 텍스트 애니메이션 */}
                        <motion.span
                            className="absolute -right-2 -top-2 bg-red-500 text-xs text-white
                px-2 py-1 rounded-full shadow"
                            initial={{ scale: 0 }}
                            animate={{ scale: 1 }}
                            transition={{ delay: 0.5 }}
                        >
                            NEW
                        </motion.span>
                    </motion.button>
                </div>

                {/* 게시물 그리드 */}
                {posts.length > 0 ? (
                    <motion.div
                        className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-16"
                        initial={{y: 20}}
                        animate={{y: 0}}
                    >
                        {posts.map((post) => (
                            <motion.div
                                key={post.id}
                                whileHover={{scale: 1.02}}
                                onClick={() => router.push(`/posts/${post.id}`)}
                                className="bg-white rounded-2xl p-6 shadow-lg hover:shadow-xl cursor-pointer transition-all"
                            >
                                {/* 이미지 섹션 */}
                                <div className="h-48 bg-gray-100 rounded-xl mb-4 overflow-hidden">
                                    {post.images.length > 0 ? (
                                        <img
                                            src={post.images[0]}
                                            alt={post.title}
                                            className="w-full h-full object-cover"
                                        />
                                    ) : (
                                        <div className="w-full h-full flex items-center justify-center text-gray-400">
                                            이미지 없음
                                        </div>
                                    )}
                                </div>

                                {/* 내용 섹션 */}
                                <div className="space-y-4">
                                    <h3 className="text-xl font-bold text-gray-800 truncate">
                                        {post.title}
                                    </h3>

                                    <div className="flex items-center justify-between">
                  <span className="px-3 py-1 bg-green-100 text-green-600 rounded-full text-sm">
                    {post.category === 'TOOL' ? '공구' : '전자기기'}
                  </span>
                                        <p className="text-2xl font-bold text-green-600">
                                            {post.price.toLocaleString()}원/
                                            <span className="text-lg">{post.priceType === 'HOUR' ? '시간' : '일'}</span>
                                        </p>
                                    </div>

                                    <div className="flex items-center text-gray-500 space-x-4">
                                        <div className="flex items-center">
                                            <MapPinIcon className="w-5 h-5 mr-1"/>
                                            <span>{selectedDistrict || '전 지역'}</span>
                                        </div>
                                        <span>•</span>
                                        <span>
                    {post.createdAt
                        ? new Date(post.createdAt).toLocaleDateString()
                        : '등록일 없음'}
                  </span>
                                    </div>

                                    <div className="pt-4 border-t border-gray-100">
                                        <p className="text-sm text-gray-500">
                                            이용 가능: {getAvailabilityRange(post.availabilities)}
                                        </p>
                                        <p className="text-sm text-gray-500 mt-1">
                                            조회수 {post.viewCount.toLocaleString()}
                                        </p>
                                    </div>
                                </div>
                            </motion.div>
                        ))}
                    </motion.div>
                ) : (
                    <motion.div
                        className="text-center py-20 bg-white rounded-2xl shadow-lg"
                        initial={{opacity: 0}}
                        animate={{opacity: 1}}
                    >
                        <p className="text-2xl text-gray-500">🔍 검색 결과가 없습니다</p>
                        <p className="mt-4 text-gray-400">다른 검색어로 시도해보세요</p>
                    </motion.div>
                )}

                {/* 페이징 버튼 */}
                <div className="flex items-center justify-center gap-4 mt-6">
                    <motion.button
                        onClick={handlePreviousPage}
                        disabled={page === 0}
                        className={`p-3 rounded-full border ${
                            page === 0
                                ? 'border-gray-300 text-gray-300'
                                : 'border-green-400 bg-green-100 text-green-600 hover:bg-green-200'
                        }`}
                        whileHover={{ scale: page === 0 ? 1 : 1.1 }}
                    >
                        <ChevronLeftIcon className="w-5 h-5" />
                    </motion.button>

                    <span className="text-lg font-semibold text-gray-600">
    {page + 1} / {totalPages + 1}
  </span>

                    <motion.button
                        onClick={handleNextPage}
                        disabled={page === totalPages - 1}
                        className={`p-3 rounded-full border ${
                            page === totalPages - 1
                                ? 'border-gray-300 text-gray-300'
                                : 'border-green-400 bg-green-100 text-green-600 hover:bg-green-200'
                        }`}
                        whileHover={{ scale: page === totalPages - 1 ? 1 : 1.1 }}
                    >
                        <ChevronRightIcon className="w-5 h-5" />
                    </motion.button>
                </div>

                {/* 로딩 상태 */}
                {loading && (
                    <div className="text-center py-8">
                        <div
                            className="animate-spin rounded-full h-12 w-12 border-4 border-green-500 border-t-transparent mx-auto"></div>
                    </div>
                )}
            </div>
        </motion.div>
    );
}