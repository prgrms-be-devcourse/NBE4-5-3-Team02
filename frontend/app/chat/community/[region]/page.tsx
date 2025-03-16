"use client";

import {useState, useEffect, useRef} from 'react';
import {ArrowLeftIcon, ChevronDownIcon, PaperclipIcon, SendHorizontalIcon, SignalIcon, SmileIcon} from "lucide-react";
import {useParams, useRouter} from 'next/navigation';
import {SparklesIcon, UserCircleIcon} from "@heroicons/react/24/outline";
import {HandThumbUpIcon} from "@heroicons/react/16/solid";
import {motion} from 'framer-motion';

interface Message {
    content: string;
    timestamp: string;
    senderName: string;
    region: string;
    openSessionCount: number;
}

interface Params {
    region: string;
}

export default function ChatPage() {
    const [messages, setMessages] = useState<Message[]>([]);
    const [input, setInput] = useState('');
    const ws = useRef<WebSocket | null>(null);
    const messageEndRef = useRef<HTMLDivElement>(null);
    // @ts-expect-error: 반드시 존재하는 값이라 오류 날 가능성 없음
    const params = useParams<Params>();
    const router = useRouter();

    const currentRegion = decodeURIComponent(params.region); // URL 디코딩
    const [openSessionCount, setOpenSessionCount] = useState(0);
    // WebSocket 연결 설정
    useEffect(() => {
        ws.current = new WebSocket(`ws://localhost:8080/chat?userId=${sessionStorage.getItem('user_id')}`);

        ws.current.onmessage = (event) => {
            try {
                const newMessage: Message = JSON.parse(event.data);
                // openSessionCount 업데이트
                setOpenSessionCount(newMessage.openSessionCount);
                setMessages((prev) => [...prev, newMessage]);
            } catch (error) {
                console.error("메시지 파싱 오류:", error);
            }
        };

        return () => {
            ws.current?.close();
        };
    }, []);

    // 날짜 포맷팅 유틸 함수 추가
    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return {
            dateHeader: date.toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                weekday: 'long'
            }),
            time: date.toLocaleTimeString('ko-KR', {
                hour: 'numeric',
                minute: '2-digit',
                hour12: true
            }).replace('오전', '오전').replace('오후', '오후')
        };
    };

    // 메시지 전송 처리
    const sendMessage = (e: React.FormEvent) => {
        e.preventDefault();
        if (input.trim() && ws.current?.readyState === WebSocket.OPEN) {
            const newMessage = {
                content: input,
                senderName: sessionStorage.getItem('nickname'),
                region: currentRegion // 실제 구현시 동적 값 사용
            };
            ws.current.send(JSON.stringify(newMessage));
            setInput('');
        }
    };

    // 스크롤 자동 이동
    useEffect(() => {
        messageEndRef.current?.scrollIntoView({behavior: 'smooth'});
    }, [messages]);

    return (
        <div className="flex flex-col h-screen bg-gradient-to-b from-blue-50 to-purple-100">
            {/* 밝은 톤의 그라데이션 헤더 */}
            <header
                className="p-4 bg-white/95 backdrop-blur-md border-b border-purple-100 shadow-sm hover:shadow-lg transition-shadow duration-300">
                <div className="max-w-4xl mx-auto flex items-center justify-between gap-4">
                    <div className="flex items-center gap-3">
                        <button
                            onClick={() => router.push('/chat/community')}
                            className="p-2 hover:bg-purple-50 rounded-xl transition-all
                   duration-200 transform hover:scale-105 active:scale-95"
                        >
                            <ArrowLeftIcon className="w-6 h-6 text-purple-600 hover:text-purple-700"/>
                        </button>

                        <div className="flex flex-col space-y-1">
                            <h1 className="text-xl font-bold text-purple-900 flex items-center gap-2">
                                <SparklesIcon className="w-5 h-5 text-yellow-400 animate-pulse"/>
                                <span
                                    className="bg-gradient-to-r from-purple-600 to-blue-500 bg-clip-text text-transparent">
              {currentRegion} 채팅
            </span>
                            </h1>
                            <div className="flex items-center gap-2">
                                <div className="flex items-center">
                                    <SignalIcon className="w-4 h-4 text-green-600 mr-1 animate-pulse"/>
                                    <span className="text-sm font-medium text-green-700">
                {openSessionCount}
              </span>
                                </div>
                                <span className="text-sm text-purple-600">
              명이 접속 중
            </span>
                            </div>
                        </div>
                    </div>

                    {/* 사용자 프로필 섹션 */}
                    <div className="relative group">
                        <div
                            className="flex items-center gap-3 cursor-pointer p-2 rounded-lg hover:bg-purple-50 transition-colors">
                            <div className="relative">
                                <div className="absolute -right-0 -top-0.5">
              <span className="flex h-3 w-3">
                <span
                    className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-3 w-3 bg-green-500"></span>
              </span>
                                </div>
                                <div
                                    className="w-10 h-10 bg-gradient-to-br from-purple-500 to-blue-400 rounded-full flex items-center justify-center text-white font-medium shadow-lg">
                                    <UserCircleIcon className="w-6 h-6 stroke-current"/>
                                </div>
                            </div>
                            <ChevronDownIcon
                                className="w-5 h-5 text-purple-600 group-hover:text-purple-700 transition-colors"/>
                        </div>

                        {/* 프로필 드롭다운 메뉴 */}
                        <div
                            className="absolute right-0 top-14 hidden group-hover:block w-48 bg-white rounded-lg shadow-xl border border-purple-50 animate-slide-down">
                            <div className="p-2 space-y-1">
                                <button
                                    className="w-full px-3 py-2 text-left text-sm text-purple-900 hover:bg-purple-50 rounded-md transition-colors">
                                    프로필 설정
                                </button>
                                <button
                                    className="w-full px-3 py-2 text-left text-sm text-purple-900 hover:bg-purple-50 rounded-md transition-colors">
                                    알림 설정
                                </button>
                                <button
                                    className="w-full px-3 py-2 text-left text-sm text-red-500 hover:bg-red-50 rounded-md transition-colors">
                                    로그아웃
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </header>

            {/* 채팅 내용*/}
            <main className="flex-1 overflow-y-auto p-4 space-y-4 max-w-4xl mx-auto w-full scroll-smooth">
                {messages.map((message, index) => {
                    const isCurrentUser = message.senderName === sessionStorage.getItem('nickname');
                    const timestamp = formatDate(message.timestamp).dateHeader;

                    return (
                        <motion.div
                            key={index}
                            initial={{opacity: 0, y: 10}}
                            animate={{opacity: 1, y: 0}}
                            className={`flex flex-col ${
                                isCurrentUser ? "items-end" : "items-start"
                            } space-y-2 group`}
                        >
                            {/* 상대방 메시지 헤더 */}
                            {!isCurrentUser && (
                                <div className="flex items-center space-x-2 pl-2">
                                    <div
                                        className="w-6 h-6 bg-gradient-to-br from-purple-400 to-blue-300 rounded-full flex items-center justify-center text-white text-xs font-bold">
                                        {message.region.slice(0, 1)}
                                    </div>
                                    <div className="flex items-baseline gap-2">
              <span className="text-sm font-semibold text-purple-700 dark:text-purple-300">
                {message.region}
              </span>
                                        <span className="text-xs text-gray-500 dark:text-gray-400">
                {message.senderName}
              </span>
                                    </div>
                                </div>
                            )}

                            {/* 메시지 버블 */}
                            <div className={`relative flex items-end gap-2 ${
                                isCurrentUser ? 'flex-row-reverse' : 'flex-row'
                            }`}>
                                <div
                                    className={`px-4 py-2 rounded-2xl shadow-sm transition-all duration-200 ${
                                        isCurrentUser
                                            ? "bg-gradient-to-br from-purple-600 to-blue-500 text-white"
                                            : "bg-gray-100 text-gray-800"
                                    } ${
                                        isCurrentUser
                                            ? 'hover:shadow-purple-sm'
                                            : 'hover:shadow-gray-sm'
                                    }`}
                                >
                                    <p className="text-base leading-relaxed tracking-wide">
                                        {message.content}
                                    </p>
                                    <span className={`absolute -bottom-5 text-xs ${
                                        isCurrentUser
                                            ? 'text-purple-500/80 right-2'
                                            : 'text-blue-400/80 left-2'
                                    } opacity-0 group-hover:opacity-100 transition-opacity`}>
            </span>
                                </div>
                                <span className={`text-xs mt-1 ${
                                    isCurrentUser ? 'text-right pr-1' : 'text-left pl-1'
                                } text-gray-500`}>
              {timestamp}
            </span>

                                {/* 반응 버튼 (호버시 표시) */}
                                <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                                    <button className="p-1 hover:bg-gray-100 dark:hover:bg-gray-600 rounded-full">
                                        <HandThumbUpIcon className="w-4 h-4 text-gray-500 dark:text-gray-400"/>
                                    </button>
                                </div>
                            </div>
                        </motion.div>

                    );
                })}
            </main>

            {/* 메시지 입력 폼 */}
            <form
                onSubmit={sendMessage}
                className="p-4 bg-white/90  backdrop-blur-lg border-t border-purple-100  shadow-sm text-gray-900"
            >
                <div className="max-w-4xl mx-auto flex gap-3 items-center">
                    <div className="flex-1 relative">
                        <input
                            type="text"
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            placeholder="메시지를 입력하세요..."
                            className="w-full pl-4 pr-20 py-3 rounded-xl border border-gray-200 text-gray-800
          focus:outline-none focus:ring-2 focus:ring-purple-500 bg-white/90
          dark:text-gray-100 transition-all duration-200"
                            maxLength={300}
                        />
                        <div className="absolute right-3 top-2.5 flex items-center gap-2">
                            <button
                                type="button"
                                className="p-1.5 hover:bg-purple-50  rounded-full transition-colors"
                            >
                                <SmileIcon className="w-5 h-5 text-purple-500 dark:text-purple-400" />
                            </button>
                            <button
                                type="button"
                                className="p-1.5 hover:bg-purple-50  rounded-full transition-colors"
                            >
                                <PaperclipIcon className="w-5 h-5 text-purple-500 dark:text-purple-400" />
                            </button>
                        </div>
                    </div>
                    <button
                        type="submit"
                        disabled={!input.trim()}
                        className="px-6 py-3 bg-gradient-to-r from-purple-600 to-blue-500 hover:from-purple-700
        hover:to-blue-600 text-white rounded-xl transition-all duration-300 transform
        hover:scale-105 active:scale-95 flex items-center gap-2 disabled:opacity-50 shadow-md"
                    >
                        <SendHorizontalIcon className="w-5 h-5" />
                        <span className="hidden sm:inline">전송</span>
                    </button>
                </div>
                <div className="flex justify-between max-w-4xl mx-auto mt-2">
                    <p className="text-xs text-gray-500 dark:text-gray-400">
                        {/* 추가 가이드라인 텍스트 */}
                        채팅은 본 커뮤니티 가이드라인을 준수합니다
                    </p>
                    <p className={`text-xs ${
                        input.length > 280 ? 'text-yellow-500' : 'text-gray-800 dark:text-gray-400'
                    }`}>
                        {input.length}/300자
                    </p>
                </div>
            </form>
        </div>
    );
}