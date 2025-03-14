'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';

interface AuthContextType {
    isLoggedIn: boolean;
    login: () => void;
    logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const router = useRouter();

    // 로그인 함수
    const login = () => {
        setIsLoggedIn(true);
        sessionStorage.getItem('access_token');
    };

    // 로그아웃 함수
    const logout = () => {
        setIsLoggedIn(false);
        sessionStorage.removeItem('access_token');
        router.push('/login'); // 로그아웃 후 로그인 페이지로 이동
    };

    // 세션 스토리지에서 로그인 상태 확인
    useEffect(() => {
        const token = sessionStorage.getItem('access_token');
        if (token) {
            setIsLoggedIn(true);
        }
    }, []);

    return (
        <AuthContext.Provider value={{ isLoggedIn, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};