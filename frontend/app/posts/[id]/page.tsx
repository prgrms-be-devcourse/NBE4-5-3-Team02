'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { motion } from 'framer-motion';
import {fetchWithAuth} from "@/app/lib/util/fetchWithAuth";
import {
    ArrowLeftIcon,
    CalendarIcon,
    ChevronLeftIcon,
    ChevronRightIcon, EyeIcon,
    MessageSquareTextIcon,
    TagIcon
} from "lucide-react";
import {
    ClipboardDocumentIcon,
    ExclamationCircleIcon,
    HandRaisedIcon,
    PhotoIcon,
    Squares2X2Icon
} from "@heroicons/react/16/solid";
import {UserCircleIcon} from "@heroicons/react/24/outline";

interface Availability {
  date: string | null;
  startTime: string;
  endTime: string;
  recurrenceDays: number;
  recurring: boolean;
}

interface PostDetail {
    id: number;
    userid: string;
    nickname: string;
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

interface DateCardProps {
    date: string;
    days?: number[];
    startTime: string;
    endTime: string;
    recurring: boolean;
}

export default function PostDetailPage() {
    const {id} = useParams();
    const router = useRouter();
    const [post, setPost] = useState<PostDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [currentImageIndex, setCurrentImageIndex] = useState(0);

    const BASE_URL = process.env.NEXT_PUBLIC_BASE_URL;
    useEffect(() => {
        const fetchPostDetail = async () => {
            try {
                const response = await fetchWithAuth(`${BASE_URL}/api/v1/posts/${id}`, {
                    method: 'GET',
                    credentials: 'include',
                    headers: {'Content-Type': 'application/json'},
                });

                if (!response.ok) throw new Error('게시물을 불러오는 데 실패했습니다.');

                const data = await response.json();
                console.log("넘어온 데이터 확인: ", data.data);
                setPost(data.data);
            } catch (err) {
                setError(err instanceof Error ? err.message : '알 수 없는 오류 발생');
            } finally {
                setLoading(false);
            }
        };

    fetchPostDetail();
  }, [id]);

    // ✅ 반복되지 않는 예약 날짜 정리
    const nonRecurringDates = post?.availabilities.filter(a => !a.recurring) || [];

    // ✅ 반복되는 예약 날짜 정리
    const recurringDates = post?.availabilities.filter(a => a.recurring) || [];

    // ✅ 요일 변환 함수 (1=월요일, 7=일요일)
    const getDayOfWeek = (dayNumber: number) => {
        const days = ["월", "화", "수", "목", "금", "토", "일"];
        return days[dayNumber - 1] || "알 수 없음"; // 1~7 외의 값이 들어오면 기본값
    };

    if (loading) return <p className="text-gray-600 text-center py-10">게시물을 불러오는 중...</p>;
    if (error) return <p className="text-red-600 text-center py-10">{error}</p>;

    const InfoRow = ({ label, value, icon }) => (
        <div className="flex items-center justify-between p-4 bg-emerald-50 rounded-xl border border-emerald-100">
            <div className="flex items-center gap-x-2 text-gray-600">
                {icon}
                <span className="font-medium">{label}</span>
            </div>
            <span className="text-gray-700 font-semibold">{value}</span>
        </div>
    );

    const InfoCard = ({ label, value, icon }) => (
        <div className="p-5 bg-white rounded-xl border-2 border-emerald-100 hover:border-emerald-200 transition-colors">
            <div className="flex items-center gap-x-3 mb-2">
                <div className="p-2 bg-emerald-100 rounded-lg text-emerald-600">
                    {icon}
                </div>
                <span className="text-sm text-gray-600">{label}</span>
            </div>
            <p className="text-2xl font-bold text-gray-800">{value}</p>
        </div>
    );

    const DateCard = ({ date, days = [], startTime, endTime, recurring }: DateCardProps) => (
        <div className="p-4 bg-white rounded-lg border border-emerald-100 hover:border-emerald-300 transition-colors">
            <div className="flex justify-between items-start">
                <div>
                    {recurring ? (
                        <span className="text-emerald-600 font-semibold">
    매주 {
                            (() => {
                                try {
                                    const validDays = Array.isArray(days)
                                        ? days.map(d => Number(d))
                                            .filter(d => !isNaN(d) && d >= 0 && d <= 6)
                                        : [];
                                    return validDays.map(getDayOfWeek).join(', ') || '요일 미지정';
                                } catch {
                                    return '요일 정보 오류';
                                }
                            })()
                        }
  </span>
                    ) : (
                        <span className="text-gray-800 font-semibold">
    {date ? new Date(date).toLocaleDateString() : '날짜 미지정'}
  </span>
                    )}
                    <span className="block text-sm text-gray-600 mt-1">
          {startTime.split(' ')[1]} - {endTime.split(' ')[1]}
        </span>
                </div>
                {/* 예약하기 버튼 (예약 페이지로 이동) */}
                <button
                    className="text-emerald-600 hover:text-emerald-700 text-sm px-3 py-1 rounded-md bg-emerald-50 transition-colors"
                    onClick={() => router.push(`/posts/${id}/reservation`)}>

                    예약하기
                </button>
            </div>
        </div>
    )

    return (
        <motion.div
            initial={{opacity: 0, scale: 0.95}}
            animate={{opacity: 1, scale: 1}}
            transition={{
                opacity: {duration: 0.3},
                scale: {type: "spring", stiffness: 300, damping: 20}
            }}
            className="w-full max-w-6xl mx-auto bg-white rounded-xl shadow-xl p-6 my-16 h-auto"
        >
            {/* 상단 헤더 섹션 */}
            <div className="flex justify-between items-start mb-8">
                <button
                    onClick={() => router.back()}
                    className="flex items-center text-gray-500 hover:text-gray-700 transition-colors"
                >
                    <ArrowLeftIcon className="w-5 h-5 mr-1"/>
                    <span className="text-lg">뒤로가기</span>
                </button>
                <span className="text-sm text-gray-500">
                {post?.createdAt && new Date(post.createdAt).toLocaleDateString()} 등록
            </span>
            </div>

            {/* 메인 콘텐츠 그리드 */}
            <div className="grid grid-cols-1 lg:grid-cols-[1fr_400px] gap-8">
                {/* 왼쪽 컬럼: 이미지 & 설명 */}
                <div>
                    <div className="relative rounded-xl overflow-hidden bg-gray-100 aspect-square mb-6 group">
                        {/* 이미지 슬라이드 컨테이너 */}
                        <div className="relative h-full w-full">
                            {post?.images?.map((img, index) => (
                                <div
                                    key={index}
                                    className={`absolute inset-0 transition-opacity duration-500 ease-in-out ${
                                        index === currentImageIndex ? 'opacity-100' : 'opacity-0'
                                    }`}
                                >
                                    <img
                                        src={img}
                                        alt={`${post.title} - ${index + 1}`}
                                        className="w-full h-full object-cover"
                                    />
                                </div>
                            ))}

                            {/* 이미지 없을 경우 */}
                            {!post?.images?.length && (
                                <div className="w-full h-full flex items-center justify-center text-gray-400">
                                    <PhotoIcon className="w-16 h-16" />
                                </div>
                            )}
                        </div>

                        {/* 네비게이션 컨트롤 */}
                        {post?.images?.length > 1 && (
                            <>
                                {/* 좌우 화살표 */}
                                <button
                                    onClick={() => setCurrentImageIndex(prev =>
                                        (prev - 1 + post.images.length) % post.images.length
                                    )}
                                    className="absolute left-2 top-1/2 -translate-y-1/2 bg-white/80 p-2 rounded-full hover:bg-white transition-colors shadow-lg"
                                >
                                    <ChevronLeftIcon className="w-6 h-6 text-gray-700" />
                                </button>
                                <button
                                    onClick={() => setCurrentImageIndex(prev =>
                                        (prev + 1) % post.images.length
                                    )}
                                    className="absolute right-2 top-1/2 -translate-y-1/2 bg-white/80 p-2 rounded-full hover:bg-white transition-colors shadow-lg"
                                >
                                    <ChevronRightIcon className="w-6 h-6 text-gray-700" />
                                </button>

                                {/* 인디케이터 도트 */}
                                <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
                                    {post.images.map((_, index) => (
                                        <button
                                            key={index}
                                            onClick={() => setCurrentImageIndex(index)}
                                            className={`w-2 h-2 rounded-full transition-colors ${
                                                index === currentImageIndex
                                                    ? 'bg-white'
                                                    : 'bg-white/50 hover:bg-white/70'
                                            }`}
                                        />
                                    ))}
                                </div>
                            </>
                        )}
                    </div>

                    {/* 상품 설명 섹션 */}
                    <div className="space-y-6">
                        {/* 아이콘과 제목을 같은 행에 배치 */}
                        <div className="flex items-center gap-x-3">
                            <ClipboardDocumentIcon className="w-8 h-8 text-emerald-600 flex-shrink-0" />
                            <h2 className="text-2xl font-bold bg-gradient-to-r from-emerald-600 to-green-500 bg-clip-text text-transparent inline-block">
                                상품 상세 정보
                            </h2>
                        </div>

                        {/* 설명 텍스트 영역 */}
                        <p className="text-gray-600 leading-relaxed whitespace-pre-line text-lg border-t border-emerald-100 pt-4">
                            {post?.content || (
                                <span className="text-emerald-500/80 flex items-center gap-x-2">
        <ExclamationCircleIcon className="w-5 h-5" />
        등록된 상세 설명이 없습니다
      </span>
                            )}
                        </p>
                    </div>
                </div>

                {/* 오른쪽 컬럼: 정보 박스 */}
                <div className="bg-gray-50 p-6 rounded-xl shadow-inner border border-gray-100">
                    <div className="mb-10 space-y-6">
                        {/* 제목 & 가격 섹션 */}
                        <div className="space-y-4">
                            <h1 className="text-4xl font-bold text-gray-800 flex items-center gap-x-2">
                                <TagIcon className="w-8 h-8 text-emerald-600" />
                                <span className="bg-gradient-to-r from-emerald-600 to-green-500 bg-clip-text text-transparent">
        {post?.title}
      </span>
                            </h1>

                            {/* 가격 카드 */}
                            <div className="p-6 bg-gradient-to-br from-emerald-50 to-green-50 rounded-2xl border-2 border-emerald-100 shadow-[0_4px_24px_rgba(16,185,129,0.1)]">
                                <p className="text-3xl font-bold text-emerald-700">
                                    {post?.price?.toLocaleString() || '가격 협의'}
                                    <span className="text-base ml-3 font-medium text-gray-600">
          ({post?.priceType || '직접협의'})
        </span>
                                </p>
                            </div>
                        </div>

                        {/* 채팅 버튼 & 작성자 정보 */}
                        <div className="space-y-3">
                            <InfoRow
                                label="카테고리"
                                value={post?.category || '기타'}
                                icon={<Squares2X2Icon className="w-5 h-5"/>}
                            />
                            <InfoRow
                                label="작성자"
                                value={post?.nickname || '알 수 없음'}
                                icon={<UserCircleIcon className="w-5 h-5"/>}
                            />

                            <button
                                onClick={() => {
                                    if(post?.userid && post?.nickname) {
                                        router.push(
                                            `/chat?userId=${encodeURIComponent(post.userid)}&nickname=${encodeURIComponent(post.nickname)}`
                                        )
                                    }
                                }}
                                className="w-full bg-emerald-600 hover:bg-emerald-700 text-white font-semibold py-4 rounded-xl transition-all duration-300 hover:shadow-lg flex items-center justify-center gap-x-2"
                            >
                                <MessageSquareTextIcon className="w-6 h-6" />
                                <span>채팅으로 문의하기</span>
                            </button>
                        </div>

                        {/* 상세 정보 그리드 */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <InfoCard
                                label="거래 방식"
                                value="직거래 가능"
                                icon={<HandRaisedIcon className="w-6 h-6"/>}
                            />
                            <InfoCard
                                label="조회수"
                                value={`${post?.viewCount || 0}회`}
                                icon={<EyeIcon className="w-6 h-6"/>}
                            />
                        </div>
                    </div>


                    {/* 예약 일정 섹션 */}
                    <div className="border-t pt-6">
                        <h3 className="text-xl font-semibold mb-4 flex items-center text-gray-600">
                            <CalendarIcon className="w-5 h-5 mr-2 text-green-500"/>
                            예약 가능 일정
                        </h3>

                        <div className="space-y-4">
                            {nonRecurringDates.map((date, index) => (
                                <DateCard
                                    key={`fixed-${index}`}
                                    date={date.date}
                                    startTime={date.startTime}
                                    endTime={date.endTime}
                                />
                            ))}

                            {recurringDates.map((date, index) => (
                                <DateCard
                                    key={`recur-${index}`}
                                    recurring
                                    days={date.recurrenceDays}
                                    startTime={date.startTime}
                                    endTime={date.endTime}
                                />
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </motion.div>
    );
}