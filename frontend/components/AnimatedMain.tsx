'use client';

import { motion } from 'framer-motion';

export default function AnimatedMain({ children }: { children: React.ReactNode }) {
    return (
        <motion.main
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 2 }}
            style={{
                background: 'linear-gradient(135deg, #d4f1c4, #a7e3e0)',
                minHeight: '100vh',
                width: '100%',
                overflowY: 'auto',
            }}
        >
            {children}
        </motion.main>
    )
}