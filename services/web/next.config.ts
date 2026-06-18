import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: true,

  // Self-contained server bundle for a small production Docker image.
  // Pin the trace root to this project so standalone is not nested under the
  // inferred monorepo root (otherwise server.js lands deep in the tree).
  output: "standalone",
  outputFileTracingRoot: process.cwd(),

  compiler: {
    removeConsole: process.env.NODE_ENV === "production",
  },

  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: '**',
      },
    ],
  },
};

export default nextConfig;