"use client";

import {useEffect, useState} from "react";
import {fetchWithAuth} from "@/app/lib/util/fetchWithAuth";

const ProtectedPage = () => {
    const [data, setData] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchProtectedResource = async () => {
            try {
                // fetchWithAuth를 사용하여 보호된 리소스 요청
                const response = await fetchWithAuth("http://localhost:8080/protected-resource", {
                    method: "GET",
                    credentials: 'include'
                });

                if (!response.ok) {
                    throw new Error("요청 실패: " + response.status);
                }

                const result = await response.text();
                setData(result);
            } catch (err: any) {
                setError(err.message);
            }
        };

        fetchProtectedResource();
    }, []);

    return (
        <div>
            <h1>보호된 페이지</h1>
            {error && <p style={{ color: "red" }}>오류: {error}</p>}
            {data && <p>응답 데이터: {data}</p>}
        </div>
    );
};

export default ProtectedPage;