"use client";

import Link from 'next/link';
import {motion} from 'framer-motion';
import {UserIcon} from "@heroicons/react/24/solid";
import {SearchIcon} from "lucide-react";

const districts = ['강남구', '노원구', '성동구', '관악구', '마포구'];

export default function Home() {
    return (
        <div className="min-h-screen bg-gradient-to-b from-blue-50 to-purple-100 p-8">

            <h1 className="text-4xl font-bold text-center mb-12 text-indigo-600">
                💬 우리 지역 커뮤니티
            </h1>

            <div className="mb-8 max-w-2xl mx-auto">
                <div className="relative">
                    <input
                        type="text"
                        placeholder="찾고 싶은 지역명을 입력하세요..."
                        className="text-gray-700 w-full px-4 py-3 rounded-lg border focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <SearchIcon className="w-5 h-5 absolute right-3 top-3.5 text-gray-700"/>
                </div>

                <div className="mt-4 flex flex-wrap gap-2 justify-center">
                    {['인기순', '최신활동순', '가나다순'].map((sort) => (
                        <button
                            key={sort}
                            className="px-3 py-1 bg-gray-300 rounded-full hover:bg-gray-400 transition-colors"
                        >
                            {sort}
                        </button>
                    ))}
                </div>
            </div>

            <div className="text-center mb-5">
                <div className="inline-grid grid-cols-3 gap-8 bg-white p-4 rounded-xl shadow-md">
                    <div>
                        <p className="text-2xl font-bold text-indigo-600">1,240</p>
                        <p className="text-gray-600">오늘의 메시지</p>
                    </div>
                    <div>
                        <p className="text-2xl font-bold text-green-600">192</p>
                        <p className="text-gray-600">활동 중인 회원</p>
                    </div>
                    <div>
                        <p className="text-2xl font-bold text-purple-600">5</p>
                        <p className="text-gray-600">개설된 커뮤니티</p>
                    </div>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-6xl mx-auto">
                {districts.map((region, index) => (
                    <motion.div
                        key={region}
                        initial={{opacity: 0, y: 20}}
                        animate={{opacity: 1, y: 0}}
                        transition={{delay: index * 0.1}}
                    >

                        <div className="relative">
                            <div className="absolute top-2 right-2 flex items-center space-x-1 text-sm text-gray-500">
                                <UserIcon className="w-4 h-4"/>
                                <span>{Math.floor(Math.random() * 100)}</span>
                            </div>

                            <div className="flex items-center mt-4 space-x-2 text-sm">
    <span className="bg-green-100 text-green-800 px-2 py-1 rounded-full">
      🆕 최근 24시간 활동
    </span>
                                <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded-full">
      💬 {Math.floor(Math.random() * 50)}개의 새 메시지
    </span>
                            </div>
                        </div>


                        <Link
                            href={`/chat/community/${region}`}
                            className="block bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow duration-300"
                        >
                            <div className="flex items-center space-x-4">
                                <div className="bg-indigo-100 p-3 rounded-lg">
                                    <span className="text-2xl">🗨️</span>
                                </div>
                                <h2 className="text-xl font-semibold text-gray-800">
                                    {region} 채팅방
                                </h2>
                            </div>
                            <p className="mt-4 text-gray-600">
                                {region} 주민들과 실시간으로 소통해보세요!
                            </p>
                        </Link>
                    </motion.div>
                ))}
            </div>
        </div>
    );
}