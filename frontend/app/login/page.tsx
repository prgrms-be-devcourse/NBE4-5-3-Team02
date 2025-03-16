"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/app/lib/auth-context";
import { motion } from "framer-motion";
import { LockClosedIcon, UserIcon } from "@heroicons/react/24/solid";

export default function LoginPage() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const hasFetched = useRef(false);

  const BASE_URL = process.env.NEXT_PUBLIC_BASE_URL;
  const FRONT_BASE_URL = process.env.NEXT_PUBLIC_FRONT_BASE_URL;

  // 폼 제출 핸들러
  const handleFormLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const response = await fetch(`${BASE_URL}/api/v1/users/login`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ username, password }),
      });

      const data = await response.json();

      if (response.ok) {
        login(); // AuthContext의 login 호출
        sessionStorage.setItem("sessionActive", "true"); // 세션 플래그 설정
        sessionStorage.setItem("nickname", data.data.nickname);
        sessionStorage.setItem("user_id", data.data.user_id);
        router.push("/");
      }
      if (!response.ok) {
        throw new Error(data.msg || "로그인 실패");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "알 수 없는 오류 발생");
    } finally {
      setIsLoading(false);
    }
  };

  // Google 로그인 URL 생성 + 리프레시 토큰 요청
  const googleLoginUrl = `https://accounts.google.com/o/oauth2/v2/auth?response_type=code&client_id=14635661476-clsktcbo2qdhshsd60onck423l80v223.apps.googleusercontent.com&redirect_uri=${FRONT_BASE_URL}/redirect&scope=openid%20https://www.googleapis.com/auth/userinfo.profile%20https://www.googleapis.com/auth/userinfo.email&access_type=offline&prompt=consent`;

  // 인증 코드 처리
  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const authCode = urlParams.get("code");

    if (authCode && !hasFetched.current) {
      hasFetched.current = true;
      fetch(`${BASE_URL}/login/oauth2/code/google`, {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ code: authCode }),
      })
        .then((response) => {
          if (!response.ok) {
            // throw new Error(`HTTP error! status: ${response.status}`);
          }
          return response.json();
        })
        .then((data) => {
          login(); // AuthContext의 login 호출
          sessionStorage.setItem("sessionActive", "true"); // 세션 플래그 설정

          console.log("백엔드 응답:", data);
          console.log(
            "추가정보필요 플래그:",
            data.data?.additionalInfoRequired
          );

          // 세션 스토리지에 액세스 토큰과 provider_id 저장
          sessionStorage.setItem("access_token", data.data.access_token);
          sessionStorage.setItem("user_id", data.data.user_id);
          sessionStorage.setItem("nickname", data.data.nickname);

          // 🔥 추가 정보 필요 여부 체크
          if (data.data?.additionalInfoRequired) {
            sessionStorage.setItem("requiresAdditionalInfo", "true");
            console.log("라우팅 시작: 추가 정보 페이지로 이동");
            router.push("/additional-info"); // 추가 정보 입력 페이지로 이동하기
          } else {
            login(); // AuthContext의 login 호출
            sessionStorage.removeItem("requiresAdditionalInfo");
            router.push("/"); // 메인 페이지로
          }
        })
        .catch((error) => {
          console.error("인증 코드 처리 중 오류:", error);
        });
    }
  }, [router]);

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 2 }}
      style={{
        background: "linear-gradient(135deg, #d4f1c4, #a7e3e0)",
        minHeight: "100vh",
        width: "100%",
        overflowY: "auto",
      }}
      className="flex items-center justify-center p-4"
    >
      <div className="bg-white rounded-3xl shadow-2xl w-full max-w-lg relative">
        <div className="bg-gray-100 h-8 rounded-t-3xl flex items-center px-4 space-x-2">
          <div className="w-3 h-3 rounded-full bg-red-500"></div>
          <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
          <div className="w-3 h-3 rounded-full bg-green-500"></div>
        </div>
        <motion.div
          initial={{ y: -50, opacity: 0 }}
          animate={{ y: 0, opacity: 1 }}
          transition={{ duration: 0.8, type: "spring" }}
          className="p-8"
        >
          <h1 className="text-3xl font-bold text-center text-gray-800 mb-6">
            🔐 로그인
          </h1>
          <form onSubmit={handleFormLogin} className="space-y-4">
            <div>
              <label
                htmlFor="username"
                className="block text-sm font-medium text-gray-700"
              >
                ID
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <UserIcon className="h-5 w-5 text-gray-400" />
                </div>
                <input
                  type="text"
                  id="username"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                  placeholder="ID 입력"
                />
              </div>
            </div>
            <div>
              <label
                htmlFor="password"
                className="block text-sm font-medium text-gray-700"
              >
                비밀번호
              </label>
              <div className="mt-1 relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <LockClosedIcon className="h-5 w-5 text-gray-400" />
                </div>
                <input
                  type="password"
                  id="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md leading-5 bg-white text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-green-500 focus:border-green-500 sm:text-sm"
                  placeholder="비밀번호 입력"
                />
              </div>
            </div>
            {error && <p className="text-red-600 text-sm">{error}</p>}
            <button
              type="submit"
              disabled={isLoading}
              className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
            >
              {isLoading ? "로그인 중..." : "로그인"}
            </button>
          </form>

          <div className="mt-6">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300"></div>
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">또는</span>
              </div>
            </div>
            <div className="mt-6">
              <a
                href={googleLoginUrl}
                className="w-full inline-flex justify-center py-2 px-4 border border-gray-300 rounded-md shadow-sm bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
              >
                <img
                  className="h-5 w-5"
                  src="https://www.svgrepo.com/show/475656/google-color.svg"
                  alt="Google logo"
                />
                <span className="ml-2"> Google로 로그인</span>
              </a>
            </div>
            <div className="mt-8 pt-6 border-t border-gray-100">
              <p className="text-center text-sm text-gray-600">
                아직 회원이 아니신가요?
                <a
                  href="/signup"
                  className="ml-2 text-green-600 hover:text-green-700 font-semibold underline"
                >
                  회원가입 하기
                </a>
              </p>
            </div>
          </div>
        </motion.div>
      </div>
    </motion.div>
  );
}
