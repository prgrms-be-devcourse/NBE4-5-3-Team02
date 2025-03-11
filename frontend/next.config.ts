import type { NextConfig } from "next";

/** @type {import('next').NextConfig} */
const nextConfig: NextConfig = {
  /* config options here */
};

export default nextConfig;

module.exports = {
  async headers() {
    return [
      {
        source: "/(.*)",
        headers: [
          {
            key: "Access-Control-Allow-Origin",
            value: "https://dapi.kakao.com",
          },
        ],
      },
    ];
  },
};

module.exports = {
    images: {
      remotePatterns: [
        {
          protocol: 'https',
          hostname: 'toolgetherbucket.s3.ap-northeast-2.amazonaws.com',
          port: '',
          pathname: '/profile/**',
        },
      ],
    }}
