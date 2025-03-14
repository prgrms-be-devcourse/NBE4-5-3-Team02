'use client'

import {useState} from 'react'
import Link from 'next/link'
import {useAuth} from '@/app/lib/auth-context'
import AuthButton from '@/components/AuthButton'
import EcoBadge from "@/components/EcoBadge"
import {BellIcon, ChatBubbleOvalLeftIcon} from '@heroicons/react/24/outline'
import {motion} from 'framer-motion'

export default function Header() {
    const {isLoggedIn} = useAuth()
    const [isDropdownOpen, setIsDropdownOpen] = useState(false)
    const [notifications, setNotifications] = useState([
        {id: 1, message: '새 리뷰가 등록되었어요!', read: false},
        {id: 2, message: '새로운 예약 요청이 있습니다!', read: false}
    ])

    const toggleDropdown = () => setIsDropdownOpen(!isDropdownOpen)

    const markAllAsRead = () => {
        setNotifications([])
        setIsDropdownOpen(false)
    }

    return (
        <header className="flex justify-between items-center p-4 bg-white shadow-md">
            <div className="flex items-center gap-6">
            <Link
                href="/"
                className="flex items-center gap-2 text-2xl font-bold text-green-600 hover:text-green-700 transition-colors"
            >
                Toolgether
            </Link>

            <div className="ml-6 flex gap-4">
                <Link
                    href="/posts"
                    className="text-gray-600 hover:text-green-600 font-semibold transition-colors"
                >
                    게시판
                </Link>
                <Link
                    href="#"
                    className="text-gray-600 hover:text-green-600 font-semibold transition-colors"
                >
                    지역 커뮤니티
                </Link>
            </div>
            </div>

            <nav className="flex items-center gap-6">
                <EcoBadge/>

                {/* 알림 섹션 */}
                {isLoggedIn && (
                    <div className="relative">
                        <button
                            onClick={toggleDropdown}
                            className="relative flex items-center justify-center w-10 h-10 bg-gray-100 rounded-full shadow-md hover:bg-gray-200 transition-colors"
                        >
                            <BellIcon className="w-6 h-6 text-green-600"/>
                            {notifications.length > 0 && (
                                <span
                                    className="absolute top-0 right-0 w-4 h-4 text-xs text-white bg-red-500 rounded-full flex items-center justify-center">
                                {notifications.length}
                            </span>
                            )}
                        </button>

                        {isDropdownOpen && (
                            <motion.div
                                initial={{opacity: 0, y: -10}}
                                animate={{opacity: 1, y: 0}}
                                className="absolute right-0 w-64 mt-2 bg-white rounded-lg shadow-xl border"
                            >
                                <div className="p-4">
                                    <div className="flex justify-between items-center mb-3">
                                        <h2 className="text-sm font-semibold">🔔 알림</h2>
                                        <button
                                            onClick={markAllAsRead}
                                            className="text-xs text-green-600 hover:text-green-700"
                                        >
                                            모두 읽음
                                        </button>
                                    </div>
                                    {notifications.length > 0 ? (
                                        <ul className="space-y-2">
                                            {notifications.map((notification) => (
                                                <li
                                                    key={notification.id}
                                                    className="p-2 bg-gray-50 rounded-md hover:bg-gray-100 transition-colors"
                                                >
                                                    <p className="text-sm text-gray-700">{notification.message}</p>
                                                </li>
                                            ))}
                                        </ul>
                                    ) : (
                                        <div className="text-center py-4 text-gray-400 text-sm">
                                            새로운 알림이 없습니다
                                        </div>
                                    )}
                                </div>
                            </motion.div>
                        )}
                    </div>
                )}

                {/* 채팅 섹션 */}
                {isLoggedIn && (
                    <Link
                        href="/chat"
                        className="p-2 text-gray-600 hover:text-green-600 relative"
                    >
                        <ChatBubbleOvalLeftIcon className="h-6 w-6" />
                    </Link>
                )}

                {/* 프로필 이미지 */}
                {isLoggedIn && (
                    <div
                        className="relative w-10 h-10 rounded-full bg-gray-200 overflow-hidden border-2 border-green-500">
                        <img
                            src="/user-profile.jpg"
                            alt="프로필"
                            className="w-full h-full object-cover hover:scale-105 transition-transform"
                        />
                    </div>
                )}

                <AuthButton/>
            </nav>
        </header>
    )
}