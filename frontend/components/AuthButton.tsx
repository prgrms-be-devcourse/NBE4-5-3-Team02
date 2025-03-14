'use client';

import { useState } from 'react';
import { Recycle, Leaf, ChevronDown, User, LogOut } from 'lucide-react';
import Link from 'next/link';
import { useAuth } from '@/app/lib/auth-context';

export default function AuthButton() {
    const { isLoggedIn, logout } = useAuth();
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);

    const toggleDropdown = () => {
        setIsDropdownOpen((prev) => !prev);
    };

    return (
        <div className="relative">
            {isLoggedIn ? (
                <div className="flex items-center gap-2">
                    <button
                        onClick={toggleDropdown}
                        className="flex items-center px-4 py-2 bg-green-100 text-gray-800 rounded-full hover:bg-green-200 transition-all group border border-green-200"
                    >
                        <Recycle className="w-5 h-5 mr-2 text-green-600 group-hover:rotate-180 transition-transform" />
                        <span>환경 지킴이</span>
                        <ChevronDown
                            className={`ml-2 w-4 h-4 transition-transform ${
                                isDropdownOpen ? 'rotate-180' : ''
                            }`}
                        />
                    </button>

                    {isDropdownOpen && (
                        <div className="absolute right-0 top-12 bg-white border border-green-100 rounded-lg shadow-lg w-48 overflow-hidden animate-fade-in">
                            <Link
                                href="/profile"
                                className="flex items-center px-4 py-3 hover:bg-green-50 transition-colors text-sm text-gray-800"
                            >
                                <User className="w-4 h-4 mr-2 text-green-600" />
                                내 정보
                            </Link>
                            <button
                                onClick={logout}
                                className="w-full flex items-center px-4 py-3 hover:bg-green-50 transition-colors text-sm text-gray-800 border-t border-green-100"
                            >
                                <LogOut className="w-4 h-4 mr-2 text-green-600" />
                                로그아웃
                            </button>
                        </div>
                    )}
                </div>
            ) : (
                <Link
                    href="/login"
                    className="flex items-center px-4 py-2 bg-green-50 text-gray-800 rounded-full hover:bg-green-100 transition-all border border-green-100 group"
                >
                    <Leaf className="w-5 h-5 mr-2 text-green-600 animate-pulse group-hover:scale-110 transition-transform" />
                    <span>시작하기</span>
                </Link>
            )}
        </div>
    );
}