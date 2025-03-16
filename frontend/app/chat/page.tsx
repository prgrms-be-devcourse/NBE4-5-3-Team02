"use client";

import {useEffect, useState, useRef} from "react";
import {useRouter, useSearchParams} from "next/navigation";
import {fetchWithAuth} from "@/app/lib/util/fetchWithAuth";
import {ChatBubbleOvalLeftIcon} from "@heroicons/react/24/outline";
import {motion} from "framer-motion";
import {TrashIcon} from "@heroicons/react/24/outline";

interface Message {
    sender: string;
    receiver: string;
    content: string;
    timeStamp: string;
    senderName: string;
    receiverName: string;
}

interface RsData<T> {
    code: string;
    message: string;
    data: T;
}

export default function ChatPage() {
    const router = useRouter();
    const [messages, setMessages] = useState<Message[]>([]);
    const [input, setInput] = useState("");
    const ws = useRef<WebSocket | null>(null);

    const [selectedChat, setSelectedChat] = useState<string | null>(null); // 선택된 채팅방
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const BASE_URL: string = process.env.NEXT_PUBLIC_BASE_URL as string;
    const strippedURL = BASE_URL.split("http://")[1];

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

    const [chatRooms, setChatRooms] = useState<string[]>([]); // 채팅방 목록
    useEffect(() => {
        const fetchChatRooms = async () => {
            const userId = sessionStorage.getItem("user_id"); // 사용자 ID 가져오기
            if (!userId) return;

            try {
                const response = await fetchWithAuth(`${BASE_URL}/api/chat/channels?userId=${userId}`);
                if (response.ok) {
                    const result: RsData<string[]> = await response.json();
                    console.log("채팅 목록:", result.data);
                    setChatRooms(result.data); // API 응답에서 채널 목록 설정
                } else {
                    console.error("Failed to fetch chat rooms");
                    setChatRooms([]);
                }
            } catch (error) {
                console.error("Error fetching chat rooms:", error);
                setChatRooms([]);
            }
        };

        fetchChatRooms();
    }, []);

    // 선택된 채팅방을 기준으로 채팅 내역을 가져옴
    const [lastMessage, setLastMsg] = useState("");
    const [lastSender, setLastSender] = useState<string | null>(null);
    const [senderNick, setSenderNick] = useState<string | null>(null);

    const fetchChatHistory = async (channelName: string) => {
        try {
            const response = await fetchWithAuth(`${BASE_URL}/api/chat/history?channelName=${channelName}`);
            if (response.ok) {
                const result: RsData<Message[]> = await response.json();
                console.log("채팅 내역:", result.data);
                setMessages(result.data); // 가져온 메시지를 상태에 저장

                const lastMessage = result.data.length > 0
                    ? result.data[result.data.length - 1].content
                    : "대화 내용이 없습니다";

                console.log("마지막 메시지:", lastMessage); // 확인용
                setLastMsg(lastMessage);

                // 마지막 메시지의 sender를 추출하여 상태에 저장
                if (result.data.length > 0) {
                    const lastMessageSender = result.data[result.data.length - 1].sender;
                    setLastSender(lastMessageSender);

                    const senderNickname = result.data[result.data.length - 1].senderName;
                    setSenderNick(senderNickname);
                    console.log("마지막 메시지의 sender:", lastMessageSender);
                } else {
                    setLastSender(null); // 이전 메시지가 없으면 null로 설정
                }
            } else {
                console.error("Failed to fetch chat history");
            }
        } catch (error) {
            console.error("Error fetching chat history:", error);
        }
    };

    // 채팅 나가기 버튼 클릭 핸들러
    const handleLeaveChat = async () => {
        if (!selectedChat) {
            alert("채팅방을 선택해주세요.");
            return;
        }

        try {
            const response = await fetchWithAuth(
                `${BASE_URL}/api/chat/delete?channelName=${selectedChat}`,
                {method: "DELETE"}
            );

            if (response.ok) {
                const result: RsData<boolean> = await response.json();
                console.log(result.message);

                // 채팅방 목록에서 삭제된 채널 제거
                setChatRooms((prev) => prev.filter((room) => room !== selectedChat));
                setSelectedChat(null); // 선택된 채팅방 초기화
                setMessages([]); // 메시지 초기화

                alert("채팅방이 성공적으로 삭제되었습니다.");
            } else {
                console.error("Failed to delete chat room");
                alert("채팅방 삭제에 실패했습니다.");
            }
        } catch (error) {
            console.error("Error deleting chat room:", error);
            alert("오류가 발생했습니다. 다시 시도해주세요.");
        }
    };

    const [, setUnreadCount] = useState(0); // 읽지 않은 메시지 카운트 상태

    // WebSocket 연결 설정
    useEffect(() => {
        ws.current = new WebSocket(`ws://${strippedURL}/chat?userId=${sessionStorage.getItem('user_id')}`); // 서버 WebSocket URL
        ws.current.onopen = () => console.log("WebSocket 연결 성공");
        ws.current.onmessage = (event) => {
            try {
                // JSON 데이터로 파싱 시도
                const newMessage: Message = JSON.parse(event.data);

                // JSON 형식이면 메시지 배열에 추가
                setMessages((prev) => [...prev, newMessage]);
            } catch (error) {
                // JSON 파싱 실패 시 단순 문자열로 처리
                const data = event.data;
                console.log(error);
                if (data.startsWith("읽지 않은 메시지가 ")) {
                    const countMatch = data.match(/\d+/); // 숫자 추출
                    setUnreadCount(countMatch); // 읽지 않은 메시지 카운트 상태 업데이트
                    console.log("읽지 않은 채팅의 수:", countMatch);
                } else {
                    console.warn("알 수 없는 문자열 데이터:", data);
                }
            }
        };

        ws.current.onclose = (event) => {
            console.log("WebSocket 연결 종료:", event.code, event.reason);
        };

        ws.current.onerror = (error) => {
            console.error("WebSocket 에러:", error);
        };

        return () => {
            ws.current?.close();
        };
    }, []);

    useEffect(() => {
        if (messagesEndRef.current) {
            // parentElement 대신 직접 요소 사용
            messagesEndRef.current.scrollTop = messagesEndRef.current.scrollHeight;
        }
    }, [messages]); // 메시지 변경시마다 실행

    const searchParams = useSearchParams();
    const userId = searchParams.get("userId") as string;
    const nickname = searchParams.get("nickname") as string;

    // 메시지 전송
    const sendMessage = () => {
        if (input.trim() === "") return; // 빈 메시지 방지
        if (ws.current?.readyState === 1) { // WebSocket이 OPEN 상태일 때만 전송
            const message = {
                type: "message",
                sender: sessionStorage.getItem("user_id"),
                receiver: lastSender || userId, // 이전 메시지의 sender를 receiver로 설정, 없으면 기본값 사용
                content: input,
                senderName: sessionStorage.getItem("nickname"),
                receiverName: nickname || senderNick,
            };
            ws.current.send(JSON.stringify(message));
            setInput("");
        } else {
            console.error("WebSocket is not open. Current state:", ws.current?.readyState);
        }
    };

    // 닫기 버튼 핸들러
    const handleClose = () => {
        router.push("/");
    };

    // 엔터 키 핸들러 추가
    const handleKeyPress = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            e.stopPropagation(); // 이벤트 버블링 방지 추가
            sendMessage();
        }
    };

    return (
        <div style={styles.container}>
            {/* 왼쪽 채팅방 목록 */}
            <div style={styles.chatList}>
                <div style={styles.chatListHeaderContainer}>
                    <div style={styles.windowControls}>
                        <button onClick={handleClose} style={styles.closeButton}></button>
                        <button style={styles.minimizeButton}></button>
                        <button style={styles.maximizeButton}></button>
                    </div>
                    <h2 style={styles.chatListHeader}>
                        <motion.div
                            className="flex items-center gap-2"
                            whileHover={{scale: 1.05}}
                            transition={{type: "spring", stiffness: 300}}
                            style={{width: 'fit-content'}} // 컨텐츠 너비에 맞춤
                        >
                            <ChatBubbleOvalLeftIcon
                                className="h-6 w-6 text-green-300 animate-pulse"
                            />
                            <span className="text-green-50">실시간 채팅</span>
                        </motion.div>
                    </h2>
                </div>
                {chatRooms && chatRooms.length > 0 ? (
                    chatRooms.map((room) => (
                        <div
                            key={room}
                            onClick={() => {
                                setSelectedChat(room); // 선택된 채팅방 설정
                                fetchChatHistory(room); // 채팅 내역 불러오기
                            }}
                            style={{
                                ...styles.chatRoom,
                                ...(selectedChat === room ? styles.selectedChatRoom : {}),
                            }}
                        >
                            <div className="text-sm" style={{cursor: 'pointer'}}>
                                {senderNick} : {lastMessage}
                            </div>
                        </div>
                    ))
                ) : (
                    <div style={styles.emptyChatContainer}>
                        <h3 style={styles.emptyChatHeader}>채팅을 시작해 보세요!</h3>
                        <p style={styles.emptyChatMessage}>
                            아직 참여 중인 채팅방이 없습니다.
                        </p>
                        <button style={styles.startChatButton} onClick={() => alert("새 채팅방 생성 기능")}>
                            게시물 둘러보기
                        </button>
                    </div>
                )}
            </div>

            {/* 오른쪽 채팅 화면 */}
            <div style={styles.chatArea}>
                <h1 style={{
                    ...styles.header,
                    color: '#666' // 글자 색상 변경
                }}>
                    <h2 style={{
                        width: '100%',  // 추가
                        textAlign: 'center'  // 추가
                    }}>
                        {nickname ? '' : senderNick}
                    </h2>
                    <button
                        onClick={handleLeaveChat}
                        style={styles.leaveButton}
                    >
                        <TrashIcon className="h-4 w-4"/>
                    </button>
                </h1>
                <div style={styles.chatBox} ref={messagesEndRef}>
                    {messages.map((msg, index) => {
                        const currentDate = formatDate(msg.timeStamp).dateHeader;
                        const prevDate = index > 0 ? formatDate(messages[index - 1].timeStamp).dateHeader : '';
                        const isOutgoing = msg.sender === sessionStorage.getItem("user_id");

                        return (
                            <div key={index}>
                                {currentDate !== prevDate && (
                                    <div style={styles.dateContainer}>
                                        <div style={styles.dateDivider}>
                                            {currentDate}
                                        </div>
                                    </div>
                                )}
                                <div style={{
                                    ...styles.messageContainer,
                                    flexDirection: isOutgoing ? 'row-reverse' : 'row'
                                }}>
                                    <div style={{
                                        ...styles.messageBubble,
                                        ...(isOutgoing ? styles.outgoingMessage : styles.incomingMessage), // 조건 반전
                                    }}>
                                        {msg.content}
                                        <div style={{
                                            ...styles.timeStamp,
                                            ...(isOutgoing ? styles.outgoingTime : styles.incomingTime)
                                        }}>
                                            {formatDate(msg.timeStamp).time}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
                <div style={styles.inputContainer}>
                    <input
                        type="text"
                        placeholder="메시지를 입력하세요"
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        onKeyDown={handleKeyPress}
                        style={styles.input}
                    />
                    <button onClick={sendMessage} style={styles.sendButton}>
                        전송
                    </button>
                </div>
            </div>
        </div>
    );
}

const styles = {
    container: {
        display: "flex" as const,
        height: "90vh",
        width: "100%",
        maxWidth: "1000px",
        backgroundColor: "#f2f2f7",
        margin: "50px auto",
        borderRadius: "10px",
        overflow: "hidden",
        boxShadow: "0 10px 25px rgba(0,0,0,0.2)",
        border: "1px solid rgba(0,0,0,0.1)",
    },
    chatList: {
        width: "30%",
        backgroundColor: "#1a1a1a",
        padding: "0",
        overflowY: "auto",
        borderRight: "1px solid #2d2d2d",

        // 신규 스크롤바 디자인
        scrollbarWidth: "thin",
        scrollbarColor: "#666 #2d2d2d",
    },
    chatListHeaderContainer: {
        padding: "15px",
        position: "relative" as const,
        borderBottom: "1px solid #333333", // Tailwind green-800
        backdropFilter: "blur(8px)",
        "&:hover": {
            backgroundColor: "rgba(51, 51, 51, 0.1)" // Tailwind green-700 with opacity
        }
    },
    chatListHeader: {
        fontSize: "1.25rem",
        fontWeight: 600,
        color: "#f0fdf4",
        padding: "12px 15px",
        display: "flex",
        alignItems: "center",
        justifyContent: "center", // 중앙 정렬 추가
        width: "100%", // 전체 너비 적용
        gap: "8px",
        transition: 'all 0.3s ease',
        '&:hover': {
            background: 'linear-gradient(to right, #166534 0%, #14532d 100%)'
        }
    },
    windowControls: {
        display: "flex" as const,
        gap: "6px",
        position: "absolute" as const,
        left: "15px",
        top: "15px",
    },
    closeButton: {
        width: "12px",
        height: "12px",
        borderRadius: "50%",
        backgroundColor: "#ff5f57",
        border: "none",
        cursor: "pointer" as const,
    },
    minimizeButton: {
        width: "12px",
        height: "12px",
        borderRadius: "50%",
        backgroundColor: "#febc2e",
        border: "none",
        cursor: "pointer" as const,
    },
    maximizeButton: {
        width: "12px",
        height: "12px",
        borderRadius: "50%",
        backgroundColor: "#28c840",
        border: "none",
        cursor: "pointer" as const,
    },
    chatRoom: {
        padding: "12px 15px",
        cursor: "pointer" as const,
        marginBottom: "0",
        borderBottom: "1px solid #2c2c2e",
        display: "flex" as const,
        alignItems: "center" as const,
    },
    selectedChatRoom: {
        backgroundColor: "#34c759",
    },
    chatArea: {
        flexGrow: 1,
        display: "flex" as const,
        flexDirection: "column" as const,
        backgroundColor: "#fff",
    },
    header: {
        textAlign: "center" as const,
        padding: "12px",
        borderBottom: "1px solid #e5e5ea",
        fontWeight: "600",
        fontSize: "20px",
        backgroundColor: "#f2f2f7",
        margin: "0",
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
    },
    chatBox: {
        flexGrow: 1,
        overflowY: "auto" as const,
        padding: "15px",
        display: "flex" as const,
        flexDirection: "column" as const,
        backgroundColor: "#fff",
    },
    dateContainer: {
        display: 'flex',
        justifyContent: 'center',
        margin: '20px 0',
    },
    dateDivider: {
        backgroundColor: '#e0e0e0',
        borderRadius: '15px',
        padding: '6px 12px',
        fontSize: '12px',
        color: '#666',
    },
    messageContainer: {
        display: 'flex',
        alignItems: 'flex-end',
        margin: '4px 0',
    },
    timeStamp: {
        fontSize: '11px',
        color: '#666',
        margin: '0 4px',
        whiteSpace: 'nowrap',
    },
    outgoingTime: {
        order: -1,
        marginRight: '8px',
    },
    incomingTime: {
        marginLeft: '8px',
    },
    // 기존 messageBubble 수정
    messageBubble: {
        maxWidth: '70%',
        padding: '10px 15px',
        borderRadius: '20px',
        marginBottom: '6px',
        fontSize: '15px',
        lineHeight: '1.4',
        wordBreak: 'break-word' as const,
        display: 'flex',
        alignItems: 'flex-end',
        gap: '8px',
    },
    incomingMessage: {
        backgroundColor: "#e5e5ea",
        alignSelf: "flex-start" as const,
        color: "#000",
        borderTopLeftRadius: "5px",
    },
    outgoingMessage: {
        backgroundColor: "#34c759",
        alignSelf: "flex-end" as const,
        color: "#fff",
        borderTopRightRadius: "5px",
    },
    inputContainer: {
        display: "flex" as const,
        gap: "10px",
        padding: "10px 15px",
        backgroundColor: "#f2f2f7",
        borderTop: "1px solid #e5e5ea",
        alignItems: "center" as const,
    },
    input: {
        flexGrow: 1,
        padding: "10px 15px",
        borderRadius: "20px",
        border: "1px solid #e5e5ea",
        color: '#374151',
        fontSize: "15px",
        outline: "none",
        backgroundColor: "#fff",
        fontFamily: "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif",
    },
    sendButton: {
        padding: "0.5rem 1rem",
        borderRadius: "20px",
        backgroundColor: "#34c759", // 변경: 파란색에서 초록색으로
        color: "#fff",
        border: "none",
        cursor: "pointer",
        fontSize: "16px",
        fontWeight: "bold",
    },
    emptyChatContainer: {
        display: "flex",
        flexDirection: "column",
        alignItems: "center", // 중앙 정렬 (수평)
        justifyContent: "center", // 중앙 정렬 (수직)
        height: "100%", // 부모 컨테이너의 높이를 채우도록 설정
        textAlign: "center", // 텍스트를 중앙 정렬
    },
    emptyChatHeader: {
        fontSize: "24px",
        fontWeight: "bold",
        marginBottom: "10px",
        color: "#fff", // 글자 색상
    },
    emptyChatMessage: {
        fontSize: "16px",
        color: "#fff",
        marginBottom: "20px",
        textAlign: "center",
    },
    startChatButton: {
        padding: "10px 20px",
        fontSize: "16px",
        color: "#fff",
        backgroundColor: "#34c759",
        border: "none",
        borderRadius: "5px",
        cursor: "pointer",
    },
    leaveButton: {
        marginLeft: "auto",
        padding: "8px 16px",
        backgroundColor: "rgba(255, 95, 87, 0.1)",
        color: "#ff5f57",
        border: "1px solid rgba(255, 95, 87, 0.3)",
        borderRadius: "20px",
        cursor: "pointer",
        display: "flex",
        alignItems: "center",
        gap: "6px",
        transition: "all 0.2s ease",
        "&:hover": {
            backgroundColor: "rgba(255, 95, 87, 0.2)",
            transform: "scale(1.05)",
            boxShadow: "0 2px 8px rgba(255, 95, 87, 0.15)"
        },
        "&:active": {
            transform: "scale(0.95)"
        }
    },
};