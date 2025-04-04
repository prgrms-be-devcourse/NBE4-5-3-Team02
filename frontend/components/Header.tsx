"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import Image from "next/image";
import { useAuth } from "@/app/lib/auth-context";
import AuthButton from "@/components/AuthButton";
import EcoBadge from "@/components/EcoBadge";
import { BellIcon, ChatBubbleOvalLeftIcon } from "@heroicons/react/24/outline";
import { motion } from "framer-motion";
import { useRouter } from "next/navigation";

interface RsData<T> {
  code: string;
  message: string;
  data: T;
}

export default function Header() {
  const { isLoggedIn } = useAuth();

  const [profile, setProfile] = useState<string>();

  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [notifications, setNotifications] = useState<
    { id: number; message: string }[]
  >([]);

  const BASE_URL = process.env.NEXT_PUBLIC_BASE_URL;
  const router = useRouter();

  const toggleDropdown = () => setIsDropdownOpen(!isDropdownOpen);

  const markAllAsRead = () => {
    setNotifications([]);
    setIsDropdownOpen(false);
  };

  const fetchUnreadNotifications = async (userId: number) => {
    try {
      const response = await fetch(
        `${BASE_URL}/api/v1/notifications/unread/${userId}`
      );
      if (response.ok) {
        const data = await response.json();
        console.log("Unread notifications: ", data);
        setNotifications(data);
        console.log("알림 목록:", notifications);
      } else {
        console.error("Failed to fetch unread notifications");
      }
    } catch (error) {
      console.error("Error fetching unread notifications:", error);
    }
  };

  const handleNotificationClick = async (
    notificationId: number,
    message: string
  ) => {
    try {
      await fetch(`${BASE_URL}/api/v1/notifications/${notificationId}`, {
        method: "PUT",
      });
      // 읽음 처리 성공 시, 알림 목록에서 제거하거나 상태를 변경
      setNotifications((prev) => prev.filter((n) => n.id !== notificationId));

      // 예약 ID 추출
      const reservationIdMatch = message.match(/\[(\d+)\]/);
      const reservationId = reservationIdMatch ? reservationIdMatch[1] : null;

      if (reservationId) {
        // 예약 상세 페이지 URL 생성
        const reservationDetailUrl = `/mypage/reservationDetail/${reservationId}`;
        console.log("Moving to:", reservationDetailUrl);

        // 페이지 이동
        router.push(reservationDetailUrl);
      } else {
        console.error("예약 ID를 추출할 수 없습니다.");
      }
    } catch (error) {
      console.error("Failed to mark notification as read:", error);
    }
  };

  useEffect(() => {
    if (isLoggedIn) {
      getMyProfile();
      const userId = sessionStorage.getItem("user_id");
      if (userId) {
        fetchUnreadNotifications(Number(userId));

        const eventSource = new EventSource(
          `${BASE_URL}/api/v1/sse/connect/${userId}`
        );

        eventSource.addEventListener("reservationUpdate", (event: any) => {
          const { data } = event;
          console.log("SSE data:", data);
          fetchUnreadNotifications(Number(userId));
        });

        eventSource.onerror = (error) => {
          console.error("SSE error:", error);
          eventSource.close();
        };

        console.log("SSE 연결 설정 완료");
        // 컴포넌트 언마운트 시 SSE 연결 종료
        return () => {
          eventSource.close();
        };
      }
    }
  }, [isLoggedIn]);

  const fetchHelper = async (url: string, options?: RequestInit) => {
    const accessToken = sessionStorage.getItem("access_token");
    if (accessToken) {
      return fetch(url, options);
    } else {
      return fetch(url, options);
    }
  };

  const getMyProfile = async () => {
    const getProfile = await fetchHelper(`${BASE_URL}/api/v1/users/profile`, {
      method: "GET",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (getProfile.ok) {
      const Data = await getProfile.json();
      if (Data?.code.startsWith("403")) {
        router.push("/login");
      }
      if (Data?.code !== "200-1") {
        console.error(`에러가 발생했습니다. \n${Data?.msg}`);
      }
      setProfile(Data?.data);
    } else {
      if (getProfile.status === 403) {
        router.push("/login");
      }
      console.error("Error fetching data:", getProfile.status);
    }
  };

  const [unreadCount, setUnreadCount] = useState<number | null>(null);
  const markChatsAsRead = async () => {
    setUnreadCount(0);
  };
  // API 호출을 통해 읽지 않은 메시지 개수를 가져오기
  useEffect(() => {
    async function fetchUnreadCount() {
      try {
        const user_id = sessionStorage.getItem("user_id"); // 사용자 ID 가져오기
        // API 호출
        const response = await fetch(
          `${BASE_URL}/api/chat/unread-count?userId=${user_id}`,
          {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
            },
          }
        );
        // RsData 구조에 맞게 응답 처리

        const data: RsData<number> = await response.json();
        setUnreadCount(data.data); // RsData의 data 필드에서 읽지 않은 메시지 개수 추출
        console.log("읽지 않은 메시지의 개수:", data.data);
      } catch (error) {
        console.error("읽지 않은 메시지 개수 가져오기 실패:", error);
      }
    }
    fetchUnreadCount();
  }, []);

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
            href="/chat/community"
            className="text-gray-600 hover:text-green-600 font-semibold transition-colors"
          >
            지역 커뮤니티
          </Link>
        </div>
      </div>

      <nav className="flex items-center gap-6">
        <EcoBadge />

        {/* 알림 섹션 */}
        {isLoggedIn && (
          <div className="relative">
            <button
              onClick={toggleDropdown}
              className="relative flex items-center justify-center w-10 h-10 bg-gray-100 rounded-full shadow-md hover:bg-gray-200 transition-colors"
            >
              <BellIcon className="w-6 h-6 text-green-600" />
              {notifications.length > 0 && (
                <span className="absolute top-0 right-0 w-4 h-4 text-xs text-white bg-red-500 rounded-full flex items-center justify-center">
                  {notifications.length}
                </span>
              )}
            </button>

            {isDropdownOpen && (
              <motion.div
                initial={{ opacity: 0, y: -10 }}
                animate={{ opacity: 1, y: 0 }}
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
                          onClick={() =>
                            handleNotificationClick(
                              notification.id,
                              notification.message
                            )
                          }
                        >
                          <p className="text-sm text-gray-700">
                            {notification.message}
                          </p>
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
            className="relative block"
            onClick={() => {
              // 채팅 읽음 처리 함수 호출
              markChatsAsRead();
            }}
          >
            <div className="relative flex items-center justify-center w-10 h-10 bg-gray-100 rounded-full shadow-md hover:bg-gray-200 transition-colors">
              <ChatBubbleOvalLeftIcon className="w-6 h-6 text-green-600" />
              {unreadCount !== null && unreadCount > 0 && (
                <span className="absolute top-0 right-0 w-4 h-4 text-xs text-white bg-red-500 rounded-full flex items-center justify-center">
                  {unreadCount}
                </span>
              )}
            </div>
          </Link>
        )}

        {/* 프로필 이미지 */}
        {isLoggedIn && (
          <Link
            href="/mypage"
            className="relative w-10 h-10 rounded-full bg-gray-200 overflow-hidden border-2 border-green-500"
          >
            {profile ? (
              <Image
                src={profile}
                alt="프로필"
                width={40}
                height={40}
                className="w-full h-full object-cover hover:scale-105 transition-transform"
              />
            ) : (
              <div className="w-full h-full object-cover hover:scale-105 transition-transform">
                <div className="bg-gray-200"></div>
              </div>
            )}
          </Link>
        )}

        <AuthButton />
      </nav>
    </header>
  );
}
