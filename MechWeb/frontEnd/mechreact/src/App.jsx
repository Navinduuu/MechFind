import { useEffect, useRef, useState } from 'react';
import axios from 'axios';
import { gsap } from 'gsap';
import './App.css';
import { initAllAnimations } from './gsapAnimations';

// ============================================
// Asset Imports
// ============================================
import logoMF from './assets/logoMF.jpeg';
import aiIcon from './assets/AI.png';
import aiBotImg from './assets/Aibota.jpeg';
import loginImg from './assets/login.jpeg';
import liveViewImg from './assets/ti-live-view.png';
import doneIcon from './assets/done.png';
import locationImg from './assets/location.png';

// Feature icons
import chatIcon from './assets/chat.png';
import gpsIcon from './assets/gps.png';
import bookingIcon from './assets/booking.png';
import paymentIcon from './assets/payment.png';

// Avatar images
import avatar1 from './assets/chani.png';
import avatar2 from './assets/hetti.png';
import avatar3 from './assets/podi.png';
import avatar4 from './assets/minura.png';

const MECHANIC_SPECIALTY_OPTIONS = [
    'ABS & Brake Mechanic',
    'Air Conditioning Mechanic',
    'Cooling System Mechanic',
    'Drivetrain Mechanic',
    'Auto Electrical Mechanic',
    'Emissions System Mechanic',
    'Engine Mechanic',
    'Fuel System Mechanic',
    'Transmission Mechanic',
    'Wheel & Tire Mechanic'
];

// ============================================
// Centralized API Service Layer
// ============================================
const API_BASE_URL1 = 'http://localhost:8081/api/mechfind';
const API_BASE_URL2 = 'http://localhost:8083/api/mechfind';
const NOTIFICATION_API_BASE = 'http://localhost:8082/api/mechfind/email';
const SUBSCRIPTION_API_BASE = 'http://localhost:8084/api/mechfind/subscriptions';
const PAYMENT_API_BASE = 'http://localhost:8085/api/mechfind/payments';

export const mechfindService = {
    login: async (loginData) => {
        const response = await axios.post(`${API_BASE_URL1}/auth/login`, loginData);
        return response.data;
    },
    register: async (registerData) => {
        const response = await axios.post(`${API_BASE_URL1}/auth/register`, registerData);
        return response.data;
    },
    changePassword: async (email, oldPassword, newPassword) => {
        const response = await axios.post(`${API_BASE_URL1}/auth/change-password`, { email, oldPassword, newPassword });
        return response.data;
    },
    getFeedbacks: async () => {
        const response = await axios.get(`${API_BASE_URL2}/feedback`);
        return response.data;
    },
    submitFeedback: async (feedbackPayload) => {
        const response = await axios.post(`${API_BASE_URL2}/feedback`, feedbackPayload);
        return response.data;
    },
    trackDownload: async (platform) => {
        const response = await axios.post(`${API_BASE_URL2}/track-download?platform=${platform}`);
        return response.data;
    },
    sendOtpEmail: async (emailPayload) => {
        const response = await axios.post(`${NOTIFICATION_API_BASE}/send-otp`, emailPayload);
        return response.data;
    },
    verifyOtp: async (verificationPayload) => {
        const response = await axios.post(`${NOTIFICATION_API_BASE}/verify-otp`, verificationPayload);
        return response.data;
    },
};

// Dedicated Subscription Service
export const subscriptionService = {
    subscribe: async (subscriptionData) => {
        const response = await axios.post(`${SUBSCRIPTION_API_BASE}/subscribe`, subscriptionData);
        return response.data;
    },
    getSubscriptionByUser: async (userId) => {
        const response = await axios.get(`${SUBSCRIPTION_API_BASE}/user/${userId}`);
        return response.data;
    },
    cancelSubscription: async (subscriptionId) => {
        const response = await axios.put(`${SUBSCRIPTION_API_BASE}/cancel/${subscriptionId}`);
        return response.data;
    },
    getPlans: async () => {
        const response = await axios.get(`${SUBSCRIPTION_API_BASE}/plans`);
        return response.data;
    }
};

// Dedicated Payment Service Integration
export const paymentService = {
    generateOtp: async (email) => {
        const response = await axios.post(`${PAYMENT_API_BASE}/generate-otp`, { email });
        return response.data;
    },
    sendPaymentOtpEmail: async (email, otp, amount) => {
        const response = await axios.post(`${NOTIFICATION_API_BASE}/send`, {
            recipientEmail: email,
            subject: "MechFind - Payment Authorization OTP",
            body: `Your payment authorization code is: ${otp}\n\nDo not share this code with anyone. It is required to complete your payment of $${amount}.`
        });
        return response.data;
    },
    verifyAndPay: async (otp, paymentData) => {
        const response = await axios.post(`${PAYMENT_API_BASE}/verify-and-pay?otp=${otp}`, paymentData);
        return response.data;
    },
    getPaymentHistory: async (userId) => {
        const response = await axios.get(`${PAYMENT_API_BASE}/history/${userId}`);
        return response.data;
    },
    verifyPayment: async (transactionId) => {
        const response = await axios.get(`${PAYMENT_API_BASE}/verify/${transactionId}`);
        return response.data;
    }
};

const NAV_LINKS = [
    { href: '#features', label: 'Features' },
    { href: '#roadmap', label: 'How it Works' },
    { href: '#pricing', label: 'Pricing' },
    { href: '#reviews', label: 'Reviews' },
    { href: '#about', label: 'About' },
];

const FEATURES = [
    { image: chatIcon, title: 'AI Chatbot Diagnosis', desc: 'Describe your symptoms in plain language. Our AI identifies common issues instantly with 95% accuracy.' },
    { image: gpsIcon, title: 'GPS Mechanic Search', desc: 'Your live location is used to find the nearest registered mechanics, sorted by distance and rating.' },
    { image: bookingIcon, title: 'Instant Booking', desc: 'Book appointments directly within the app. No phone calls — confirm in seconds, track arrival live.' },
    { image: paymentIcon, title: 'Secure Payments', desc: 'Pay for services safely through the app. No cash hassle on the road — fully encrypted transactions.' },
];

const SPOTLIGHT = [
    { icon: 'ti-brain', title: 'Intelligent Triage', desc: 'Our AI cross-references thousands of vehicle fault patterns to surface the most likely diagnosis from your natural-language description.', badge: ['ti-bolt', '2.1s avg. response'] },
    { icon: 'ti-shield-check', title: 'Verified Mechanics', desc: "Every mechanic on MechFind passes a background check and skills assessment. You only see professionals who've earned their badge.", badge: ['ti-certificate', '2,400+ verified'] },
    { icon: 'ti-live-view', title: 'Live Tracking', desc: "Watch your mechanic's ETA on a live map. No more waiting and wondering — you see exactly when help arrives.", badge: ['ti-gps', 'Real-time GPS'] },
    { icon: 'ti-lock', title: 'End-to-End Encrypted', desc: 'Payments, location data, and diagnostic history are protected with AES-256 encryption — your data never leaves your control.', badge: ['ti-key', 'AES-256'] },
];

const DRIVER_MILESTONES = [
    { label: '1. Describe Issue', state: 'complete' },
    { label: '2. AI Diagnosis', state: 'complete' },
    { label: '3. Match Mechanic', state: 'progress' },
    { label: '4. Instant Booking', state: 'pending' },
];

const TOW_MILESTONES = [
    { label: '1. Breakdown Reported', state: 'complete' },
    { label: '2. AI Locates Your Rig', state: 'complete' },
    { label: '3. Job Accepted', state: 'progress' },
    { label: '4. Vehicle Delivered', state: 'pending' },
];

const ROADMAP_COPY = {
    driver: { tag: 'The Process', title: 'From breakdown to fixed', sub: 'Four simple steps to get your vehicle back on the road.' },
    tow: { tag: 'For Operators', title: 'From alert to payout', sub: 'Four simple steps to pick up jobs and get paid — no dispatcher required.' },
};

const PRICING_PLANS = [
   {
        id: 'driver-basic',
        name: 'Driver Basic',
        price: '$0',
        numericPrice: 0,
        period: '/ mo',
        featured: false,
        features: ['3 AI diagnoses / month', 'Basic mechanic search', 'In-app messaging'],
        cta: 'Get Started',
    },
    {
        id: 'mechanic-basic',
        name: 'Mechanic Basic',
        price: '$0',
        numericPrice: 0,
        period: '/ mo',
        featured: false,
        features: ['3 AI diagnoses / month', 'Basic mechanic search', 'In-app messaging'],
        cta: 'Get Started',
    },
    {
        id: 'driver-pro',
        name: 'Driver Pro',
        price: '$9.99',
        numericPrice: 9.99,
        period: '/ mo',
        featured: true,
        features: ['Unlimited AI diagnoses', 'Priority mechanic match', '24/7 roadside AI support'],
        cta: 'Upgrade to Pro',
    },
    {
        id: 'mechanic-pro',
        name: 'Mechanic Pro',
        price: '$19.99',
        numericPrice: 19.99,
        period: '/ mo',
        featured: false,
        features: ['Verified listing', 'Job booking management', 'Priority placement'],
        cta: 'For Mechanics',
    },
];

const TEAM = [
    { initials: 'MLD', name: 'M L D Dananjaya', id: 'COHNDSE252F-028' },
    { initials: 'NTF', name: 'W.W.N.T Fernando', id: 'COHNDSE252F-038' },
    { initials: 'YDF', name: 'W.Y.D Fernando', id: 'COHNDSE252F-056' },
    { initials: 'INS', name: 'I.V.N.S Ilukpitiya', id: 'COHNDSE252F-057' },
];

const AUTH_COPY = {
    login: { icon: 'ti-login', title: 'Welcome Back', sub: 'Sign in to access your vehicle diagnostics.', submitIcon: 'ti-login', submitLabel: 'Sign In' },
    register: { icon: 'ti-user-plus', title: 'Join MechFind', sub: 'Create your account and verify your email.', submitIcon: 'ti-user-plus', submitLabel: 'Create Account' },
    'verify-otp': { icon: 'ti-mail-check', title: 'Verify Email', sub: 'Enter the OTP sent to your email address.', submitIcon: 'ti-shield-check', submitLabel: 'Verify & Finish' },
    'change-password': { icon: 'ti-lock-open', title: 'Change Password', sub: 'Update your account credentials safely.', submitIcon: 'ti-key', submitLabel: 'Change Password' },
};

function getPasswordStrength(pwd) {
    if (!pwd) return null;
    let score = 0;
    if (pwd.length >= 6) score++; // Minimum requirement aligned with backend @Size(min=6)
    if (pwd.length >= 8) score++;
    if (/[A-Z]/.test(pwd)) score++;
    if (/[0-9]/.test(pwd)) score++;
    if (/[^A-Za-z0-9]/.test(pwd)) score++;
    
    if (pwd.length < 6) return { label: 'Too short (min 6 chars)', color: '#ef4444', width: '20%' };
    if (score <= 2) return { label: 'Fair', color: '#f59e0b', width: '50%' };
    if (score === 3) return { label: 'Good', color: '#3b82f6', width: '75%' };
    return { label: 'Strong', color: '#22c55e', width: '100%' };
}

function BrandMark({ size = 36, radius = 10 }) {
    return (
        <span className="brand-mark" style={{ width: size, height: size, borderRadius: radius, overflow: 'hidden', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
            <img src={logoMF} alt="MechFind Logo" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
        </span>
    );
}

function App() {
    const [feedbacks, setFeedbacks] = useState([]);
    const [loadingFeedbacks, setLoadingFeedbacks] = useState(true);
    const [userType, setUserType] = useState('Mechanic');
    const [message, setMessage] = useState('');
    const [rating, setRating] = useState(5);
    const [formStatus, setFormStatus] = useState({ type: '', text: '' });

    const [theme, setTheme] = useState('light');

    const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
    const [authMode, setAuthMode] = useState('login');
    const [otpInput, setOtpInput] = useState('');
    const [pendingRegisterPayload, setPendingRegisterPayload] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);

    // Subscription & Payment State
    const [currentSubscription, setCurrentSubscription] = useState(null);
    const [selectedPlanForPayment, setSelectedPlanForPayment] = useState(null);
    const [isPaymentModalOpen, setIsPaymentModalOpen] = useState(false);
    const [paymentStep, setPaymentStep] = useState('form'); // 'form' | 'otp' | 'success'
    const [paymentLoading, setPaymentLoading] = useState(false);
    const [paymentStatus, setPaymentStatus] = useState({ type: '', text: '' });
    const [paymentFormData, setPaymentFormData] = useState({
        email: '',
        cardNumber: '',
        expiry: '',
        cvv: ''
    });
    const [paymentOtp, setPaymentOtp] = useState('');
    const [transactionId, setTransactionId] = useState('');

    const [authForm, setAuthForm] = useState({
        email: '',
        password: '',
        newPassword: '',
        oldPassword: '',
        name: '',
        phone: '',
        street: '',
        gender: 'Not Specified',
        registrationUserType: 'Mechanic',
        speciality: MECHANIC_SPECIALTY_OPTIONS[0],
        latitude: 6.7815047,
        longitude: 79.8993186
    });
    const [authStatus, setAuthStatus] = useState({ type: '', text: '' });

    const [currentUser, setCurrentUser] = useState(() => {
        try {
            const savedUser = localStorage.getItem('currentUser');
            return savedUser ? JSON.parse(savedUser) : null;
        } catch (e) {
            console.error('Failed to parse user from localStorage', e);
            return null;
        }
    });

    const [showPassword, setShowPassword] = useState(false);
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const [roadmapTab, setRoadmapTab] = useState('driver');

    const authOverlayRef = useRef(null);
    const authModalRef = useRef(null);
    const paymentOverlayRef = useRef(null);

    useEffect(() => {
        if (currentUser && currentUser.userType) {
            setUserType(currentUser.userType);
        }
    }, [currentUser]);

    useEffect(() => {
        if (currentUser) {
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
        } else {
            localStorage.removeItem('currentUser');
            setCurrentSubscription(null);
        }
    }, [currentUser]);

    // Fetch Active Subscription on User Login / Mount
    useEffect(() => {
        const fetchUserSubscription = async () => {
            const activeUserId = currentUser?.id || currentUser?.userId;
            if (activeUserId) {
                try {
                    const sub = await subscriptionService.getSubscriptionByUser(activeUserId);
                    if (sub && sub.hasSubscription !== false) {
                        setCurrentSubscription(sub);
                    } else {
                        setCurrentSubscription(null);
                    }
                } catch (e) {
                    console.warn('Subscription service unavailable or no active subscription found.');
                }
            }
        };
        fetchUserSubscription();
    }, [currentUser]);

    useEffect(() => {
        document.documentElement.setAttribute('data-theme', theme);
    }, [theme]);

    useEffect(() => {
        if ('geolocation' in navigator) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    setAuthForm((prev) => ({
                        ...prev,
                        latitude: position.coords.latitude,
                        longitude: position.coords.longitude
                    }));
                },
                (error) => {
                    console.warn('Geolocation access declined or unavailable, using defaults.', error);
                }
            );
        }
    }, []);

    const toggleTheme = () => setTheme((prev) => (prev === 'light' ? 'dark' : 'light'));

    const openAuthModal = (mode) => {
        setAuthMode(mode);
        setIsAuthModalOpen(true);
        setAuthStatus({ type: '', text: '' });
        setOtpInput('');
        setPendingRegisterPayload(null);
        setIsSubmitting(false);
        setAuthForm((prev) => ({
            ...prev,
            email: '',
            password: '',
            newPassword: '',
            oldPassword: '',
            name: '',
            phone: '',
            street: '',
            gender: 'Not Specified',
            registrationUserType: 'Mechanic',
            speciality: MECHANIC_SPECIALTY_OPTIONS[0],
        }));
        setShowPassword(false);
    };

    const closeAuthModal = () => setIsAuthModalOpen(false);
    const closeMobileMenu = () => setMobileMenuOpen(false);

    const handleAuthChange = (e) => setAuthForm({ ...authForm, [e.target.name]: e.target.value });

    const handleAuthSubmit = async (e) => {
        e.preventDefault();
        if (isSubmitting) return;

        // Client-side password validation matching backend @Size(min = 6)
        if (authMode === 'register' && authForm.password.length < 6) {
            setAuthStatus({ type: 'error', text: 'Password must be at least 6 characters' });
            return;
        }
        if (authMode === 'change-password' && authForm.newPassword.length < 6) {
            setAuthStatus({ type: 'error', text: 'Password must be at least 6 characters' });
            return;
        }

        setIsSubmitting(true);
        setAuthStatus({ type: '', text: 'Processing…' });
        try {
            if (authMode === 'login') {
                const loginPayload = { email: authForm.email, password: authForm.password };
                const userData = await mechfindService.login(loginPayload);
                setCurrentUser(userData);
                setAuthStatus({ type: 'success', text: 'Login successful!' });
                setTimeout(closeAuthModal, 1000);
            } else if (authMode === 'register') {
                const registerPayload = {
                    name: authForm.name,
                    email: authForm.email,
                    phone: authForm.phone,
                    street: authForm.street,
                    gender: authForm.gender || 'Not Specified',
                    password: authForm.password,
                    userType: authForm.registrationUserType,
                    speciality: authForm.registrationUserType === 'Mechanic' ? authForm.speciality : null,
                    latitude: Number(authForm.latitude),
                    longitude: Number(authForm.longitude)
                };

                setPendingRegisterPayload(registerPayload);

                await mechfindService.sendOtpEmail({ recipientEmail: authForm.email, purpose: 'REGISTRATION' });

                setAuthStatus({ type: 'success', text: 'OTP sent to your email address!' });
                setAuthMode('verify-otp');
            } else if (authMode === 'verify-otp') {
                if (!otpInput.trim()) {
                    setAuthStatus({ type: 'error', text: 'Please enter the OTP sent to your email.' });
                    return;
                }

                const verifyPayload = { email: pendingRegisterPayload?.email || authForm.email, otp: otpInput.trim() };
                await mechfindService.verifyOtp(verifyPayload);

                const userData = await mechfindService.register(pendingRegisterPayload);
                setCurrentUser(userData);
                setAuthStatus({ type: 'success', text: 'Email verified & registration complete!' });
                setTimeout(closeAuthModal, 1000);
            } else if (authMode === 'change-password') {
                await mechfindService.changePassword(authForm.email, authForm.oldPassword, authForm.newPassword);
                setAuthStatus({ type: 'success', text: 'Password changed successfully!' });
                setTimeout(() => {
                    setAuthMode('login');
                    setAuthStatus({ type: '', text: '' });
                    setShowPassword(false);
                }, 1500);
            }
        } catch (error) {
            console.error('Authentication Error Details:', error.response?.data || error.message);
            const responseData = error.response?.data;
            let errorMessage = 'Authentication failed. Please check your inputs.';

            if (typeof responseData === 'string') {
                errorMessage = responseData;
            } else if (responseData && typeof responseData === 'object') {
                if (Array.isArray(responseData.errors)) {
                    errorMessage = responseData.errors.map(err => err.defaultMessage || (err.field ? `${err.field}: ${err.defaultMessage}` : JSON.stringify(err))).join(', ');
                } else {
                    errorMessage = responseData.message || responseData.error || JSON.stringify(responseData);
                }
            }

            setAuthStatus({ type: 'error', text: errorMessage });
        } finally {
            setIsSubmitting(false);
        }
    };

    // ============================================
    // Subscription & Two-Step Payment Handlers
    // ============================================
    const handlePaymentInputChange = (e) => {
        let { name, value } = e.target;
        if (name === 'cardNumber') {
            value = value.replace(/\D/g, '').slice(0, 16);
        } else if (name === 'expiry') {
            value = value.replace(/\D/g, '').slice(0, 4);
            if (value.length >= 3) value = `${value.slice(0, 2)}/${value.slice(2)}`;
        } else if (name === 'cvv') {
            value = value.replace(/\D/g, '').slice(0, 4);
        }
        setPaymentFormData({ ...paymentFormData, [name]: value });
    };

    const handleInitiatePlanSelection = (plan) => {
        const activeUserId = currentUser?.id || currentUser?.userId;
        if (!currentUser || !activeUserId) {
            alert('Please sign in to choose a subscription plan.');
            openAuthModal('login');
            return;
        }

        setSelectedPlanForPayment(plan);
        setPaymentStatus({ type: '', text: '' });
        setPaymentStep('form');
        setPaymentOtp('');
        setPaymentFormData({
            email: currentUser.email || '',
            cardNumber: '',
            expiry: '',
            cvv: ''
        });
        
        if (plan.numericPrice === 0) {
            executeSubscriptionProcess(plan, null);
        } else {
            setIsPaymentModalOpen(true);
        }
    };

    const executeSubscriptionProcess = async (plan, paymentTransactionId = null) => {
        const activeUserId = currentUser?.id || currentUser?.userId || currentUser?.email;
        
        if (!activeUserId) {
            setPaymentStatus({ type: 'error', text: 'User ID missing. Please sign in again.' });
            setPaymentLoading(false);
            return;
        }

        try {
            const subscriptionPayload = {
                userId: String(activeUserId),
                planName: plan.name,
                planId: plan.id,
                amount: plan.numericPrice,
                billingCycle: 'MONTHLY',
                userType: currentUser?.userType || 'Mechanic',
                transactionId: paymentTransactionId
            };
            const subscriptionResult = await subscriptionService.subscribe(subscriptionPayload);
            
            setCurrentSubscription(subscriptionResult || subscriptionPayload);
            setPaymentStatus({ type: 'success', text: 'Subscription updated successfully!' });
            setPaymentStep('success');
        } catch (error) {
            console.error('Subscription service error:', error);
            const errMsg = error.response?.data?.message || 'Failed to save subscription. Check backend logs.';
            setPaymentStatus({ type: 'error', text: errMsg });
        } finally {
            setPaymentLoading(false);
        }
    };

    const handlePaymentSubmitStep1 = async (e) => {
        e.preventDefault();
        setPaymentLoading(true);
        setPaymentStatus({ type: '', text: '' });

        try {
            const otpData = await paymentService.generateOtp(paymentFormData.email);
            await paymentService.sendPaymentOtpEmail(paymentFormData.email, otpData.otp, selectedPlanForPayment.numericPrice);

            setPaymentStep('otp');
            setPaymentStatus({ type: 'success', text: `OTP sent to ${paymentFormData.email}` });
        } catch (err) {
            const msg = err.response?.data?.message || err.message || "Failed to generate or send OTP";
            setPaymentStatus({ type: 'error', text: msg });
        } finally {
            setPaymentLoading(false);
        }
    };

    const handlePaymentSubmitStep2 = async (e) => {
        e.preventDefault();
        setPaymentLoading(true);
        setPaymentStatus({ type: '', text: '' });

        try {
            if (paymentOtp.trim().length === 6) {
                const generatedTxnId = `TXN-${Date.now()}`;
                setTransactionId(generatedTxnId);
                await executeSubscriptionProcess(selectedPlanForPayment, generatedTxnId);
            } else {
                throw new Error("Please enter a valid 6-digit OTP code.");
            }
        } catch (err) {
            const msg = err.response?.data?.message || err.message || "Invalid OTP or Payment Failed";
            setPaymentStatus({ type: 'error', text: msg });
            setPaymentLoading(false);
        }
    };

    const handleCancelSubscription = async () => {
        const subscriptionId = currentSubscription?.id || currentSubscription?.subscriptionId || currentUser?.id;
        if (!subscriptionId) return;
        if (!window.confirm('Are you sure you want to cancel your current subscription?')) return;

        try {
            await subscriptionService.cancelSubscription(subscriptionId);
            setCurrentSubscription(null);
            alert('Your subscription has been cancelled.');
        } catch (error) {
            console.error('Cancellation error:', error);
            setCurrentSubscription(null);
            alert('Subscription removed locally.');
        }
    };

    const fetchFeedbacks = async () => {
        try {
            setLoadingFeedbacks(true);
            const data = await mechfindService.getFeedbacks();
            if (Array.isArray(data)) {
                setFeedbacks(data);
            } else if (data && Array.isArray(data.feedbacks)) {
                setFeedbacks(data.feedbacks);
            } else if (data && Array.isArray(data.data)) {
                setFeedbacks(data.data);
            } else {
                setFeedbacks([]);
            }
        } catch (error) {
            console.error('Error loading feedback:', error);
            setFeedbacks([]);
        } finally {
            setLoadingFeedbacks(false);
        }
    };

    useEffect(() => {
        fetchFeedbacks();
        const onScroll = () => {
            const nav = document.getElementById('navbar');
            if (nav) nav.classList.toggle('scrolled', window.scrollY > 50);
        };
        window.addEventListener('scroll', onScroll, { passive: true });
        return () => window.removeEventListener('scroll', onScroll);
    }, []);

    useEffect(() => {
        const cleanup = initAllAnimations();
        return cleanup;
    }, []);

    useEffect(() => {
        if (!authOverlayRef.current || !authModalRef.current) return;
        if (isAuthModalOpen) {
            gsap.to(authOverlayRef.current, { opacity: 1, duration: 0.25, ease: 'power2.out' });
            gsap.fromTo(authModalRef.current, { y: 24, opacity: 0, scale: 0.97 }, { y: 0, opacity: 1, scale: 1, duration: 0.4, ease: 'power3.out' });
        } else {
            gsap.to(authOverlayRef.current, { opacity: 0, duration: 0.2, ease: 'power2.in' });
        }
    }, [isAuthModalOpen]);

    const handleTrackDownload = async (platform) => {
        try {
            await mechfindService.trackDownload(platform);
            alert(`Download sequence initiated for: ${platform}`);
        } catch (error) {
            console.error(`Error logging download for ${platform}:`, error);
        }
    };

    const handleFeedbackSubmit = async (e) => {
        e.preventDefault();
        setFormStatus({ type: '', text: '' });

        if (!message.trim()) {
            setFormStatus({ type: 'error', text: 'Please write a brief message.' });
            return;
        }

        const activeUserId = currentUser?.id || currentUser?.userId;
        if (!currentUser || !activeUserId) {
            setFormStatus({ type: 'error', text: 'Please sign in to submit feedback.' });
            openAuthModal('login');
            return;
        }

        const feedbackPayload = {
            userId: activeUserId,
            userType: currentUser.userType || userType || 'Mechanic',
            message: message.trim(),
            rating: parseInt(rating, 10)
        };

        try {
            await mechfindService.submitFeedback(feedbackPayload);
            setFormStatus({ type: 'success', text: 'Feedback successfully recorded!' });
            setMessage('');
            setRating(5);
            fetchFeedbacks();
        } catch (error) {
            console.error('Error submitting feedback:', error);
            setFormStatus({ type: 'error', text: 'Connection to server failed.' });
        }
    };

    const pwdTarget = authMode === 'change-password' ? authForm.newPassword : authForm.password;
    const pwdStrength = (authMode === 'register' || authMode === 'change-password') ? getPasswordStrength(pwdTarget) : null;
    const copy = AUTH_COPY[authMode];
    const activeMilestones = roadmapTab === 'driver' ? DRIVER_MILESTONES : TOW_MILESTONES;
    const activeRoadmapCopy = ROADMAP_COPY[roadmapTab];

    return (
        <div className="app-container">
            <div className="bg-blob blob-1"></div>
            <div className="bg-blob blob-2"></div>
            <div className="bg-blob blob-3"></div>

            {/* Auth Modal */}
            <div
                ref={authOverlayRef}
                className={`auth-overlay${isAuthModalOpen ? ' open' : ''}`}
                onClick={(e) => { if (e.target === e.currentTarget) closeAuthModal(); }}
            >
                <div ref={authModalRef} className="auth-modal">
                    <button className="auth-close" onClick={closeAuthModal} aria-label="Close">&times;</button>
                    <div className="auth-modal-header">
                        <img src={logoMF} alt="Auth Header" style={{ width: 44, height: 44, borderRadius: 10, objectFit: 'cover', marginBottom: '0.5rem' }} />
                        <h2>{copy.title}</h2>
                        <p>{copy.sub}</p>
                    </div>

                    <div className="auth-tabs">
                        <button type="button" className={`auth-tab${authMode === 'login' ? ' active' : ''}`} onClick={() => openAuthModal('login')}>Sign In</button>
                        <button type="button" className={`auth-tab${authMode === 'register' || authMode === 'verify-otp' ? ' active' : ''}`} onClick={() => openAuthModal('register')}>Register</button>
                        <button type="button" className={`auth-tab${authMode === 'change-password' ? ' active' : ''}`} onClick={() => openAuthModal('change-password')}>Change PW</button>
                    </div>

                    <form onSubmit={handleAuthSubmit} noValidate>
                    {authMode === 'register' && (
                    <>
                        <div className="auth-form-group">
                            <label><i className="ti ti-id"></i> Registering as:</label>
                            <select name="registrationUserType" value={authForm.registrationUserType} onChange={handleAuthChange}>
                                <option value="Mechanic">Professional Mechanic</option>
                                <option value="TowTruck">Tow Truck Operator</option>
                            </select>
                        </div>

                        {authForm.registrationUserType === 'Mechanic' && (
                            <div className="auth-form-group">
                                <label><i className="ti ti-tools"></i> Mechanic Specialty</label>
                                <select name="speciality" value={authForm.speciality} onChange={handleAuthChange}>
                                    {MECHANIC_SPECIALTY_OPTIONS.map((spec) => (
                                        <option key={spec} value={spec}>
                                            {spec}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        )}

                        <div className="auth-form-group">
                            <label><i className="ti ti-user"></i> Name</label>
                            <input type="text" name="name" value={authForm.name} onChange={handleAuthChange} required placeholder="John Doe" autoComplete="name" />
                        </div>
                        <div className="auth-form-group">
                            <label><i className="ti ti-phone"></i> Phone Number</label>
                            <input type="tel" name="phone" value={authForm.phone} onChange={handleAuthChange} required placeholder="+94771234567" autoComplete="tel" />
                        </div>
                        <div className="auth-form-group">
                            <label><i className="ti ti-map-pin"></i> Street</label>
                            <input type="text" name="street" value={authForm.street} onChange={handleAuthChange} required placeholder="123 Main Street, Colombo" autoComplete="street-address" />
                        </div>
                        <div className="auth-form-group">
                            <label><i className="ti ti-gender-transgender"></i> Gender</label>
                            <select name="gender" value={authForm.gender} onChange={handleAuthChange}>
                                <option value="Not Specified">Not Specified</option>
                                <option value="Male">Male</option>
                                <option value="Female">Female</option>
                            </select>
                        </div>
                    </>
                    )}
                        {(authMode === 'login' || authMode === 'register' || authMode === 'change-password') && (
                            <div className="auth-form-group">
                                <label><i className="ti ti-mail"></i> Email Address</label>
                                <input type="email" name="email" value={authForm.email} onChange={handleAuthChange} required placeholder="example@gmail.com" autoComplete="email" />
                            </div>
                        )}

                        {authMode === 'verify-otp' && (
                            <div className="auth-form-group">
                                <label><i className="ti ti-shield-check"></i> Enter OTP Code</label>
                                <input
                                    type="text"
                                    name="otpInput"
                                    value={otpInput}
                                    onChange={(e) => setOtpInput(e.target.value)}
                                    maxLength="6"
                                    required
                                    placeholder="e.g. 54321"
                                    style={{ letterSpacing: '0.2rem', textAlign: 'center', fontSize: '1.2rem' }}
                                />
                            </div>
                        )}

                        {(authMode === 'login' || authMode === 'register') && (
                            <div className="auth-form-group">
                                <label><i className="ti ti-lock"></i> Password {authMode === 'register' && <span style={{fontSize: '0.8rem', color: 'var(--muted-foreground)'}}>(min. 6 characters)</span>}</label>
                                <div className="auth-input-wrap">
                                    <input
                                        type={showPassword ? 'text' : 'password'}
                                        name="password"
                                        value={authForm.password}
                                        onChange={handleAuthChange}
                                        required
                                        placeholder="••••••••"
                                        autoComplete={authMode === 'login' ? 'current-password' : 'new-password'}
                                    />
                                    <button type="button" className="auth-eye" onClick={() => setShowPassword((p) => !p)} aria-label="Toggle password">
                                        <i className={`ti ${showPassword ? 'ti-eye-off' : 'ti-eye'}`}></i>
                                    </button>
                                </div>
                            </div>
                        )}

                        {authMode === 'change-password' && (
                            <>
                                <div className="auth-form-group">
                                    <label><i className="ti ti-lock"></i> Current Password</label>
                                    <div className="auth-input-wrap">
                                        <input type={showPassword ? 'text' : 'password'} name="oldPassword" value={authForm.oldPassword} onChange={handleAuthChange} required placeholder="••••••••" autoComplete="current-password" />
                                        <button type="button" className="auth-eye" onClick={() => setShowPassword((p) => !p)} aria-label="Toggle password">
                                            <i className={`ti ${showPassword ? 'ti-eye-off' : 'ti-eye'}`}></i>
                                        </button>
                                    </div>
                                </div>
                                <div className="auth-form-group">
                                    <label><i className="ti ti-lock-open"></i> New Password <span style={{fontSize: '0.8rem', color: 'var(--muted-foreground)'}}>(min. 6 characters)</span></label>
                                    <input type="password" name="newPassword" value={authForm.newPassword} onChange={handleAuthChange} required placeholder="••••••••" autoComplete="new-password" />
                                </div>
                            </>
                        )}

                        {pwdStrength && pwdTarget && (
                            <div className="auth-strength">
                                <div className="auth-strength-bar">
                                    <div style={{ width: pwdStrength.width, background: pwdStrength.color }}></div>
                                </div>
                                <span style={{ color: pwdStrength.color }}>{pwdStrength.label}</span>
                            </div>
                        )}

                        {authStatus.text && (
                            <div className={`auth-status ${authStatus.type}`}>
                                <i className={`ti ${authStatus.type === 'success' ? 'ti-circle-check' : 'ti-alert-circle'}`}></i>
                                {authStatus.text}
                            </div>
                        )}

                        <button type="submit" className="auth-submit-btn" disabled={isSubmitting}>
                            <i className={`ti ${copy.submitIcon}`}></i> {isSubmitting ? 'Processing...' : copy.submitLabel}
                        </button>
                    </form>

                    {authMode === 'login' && (
                        <p className="auth-footer-note">
                            Forgot your password? <button type="button" onClick={() => openAuthModal('change-password')}>Reset it</button>
                        </p>
                    )}
                </div>
            </div>

            {/* Payment & Subscription Checkout Modal */}
            {isPaymentModalOpen && selectedPlanForPayment && (
                <div
                    ref={paymentOverlayRef}
                    className="auth-overlay open"
                    onClick={(e) => { if (e.target === e.currentTarget && !paymentLoading) setIsPaymentModalOpen(false); }}
                >
                    <div className="auth-modal payment-modal">
                        <button 
                            className="auth-close" 
                            onClick={() => setIsPaymentModalOpen(false)} 
                            aria-label="Close"
                            disabled={paymentLoading}
                        >
                            &times;
                        </button>

                        <div className="auth-modal-header text-center">
                            <h2>MechFind Checkout</h2>
                            <p style={{ marginTop: '4px' }}>
                                Total Amount: <strong>${selectedPlanForPayment.numericPrice}</strong> ({selectedPlanForPayment.name})
                            </p>
                        </div>

                        {paymentStatus.text && (
                            <div className={`auth-status ${paymentStatus.type}`} style={{ marginBottom: '1rem' }}>
                                <i className={`ti ${paymentStatus.type === 'success' ? 'ti-circle-check' : 'ti-alert-circle'}`}></i>
                                {paymentStatus.text}
                            </div>
                        )}

                        {paymentStep === 'form' && (
                            <form onSubmit={handlePaymentSubmitStep1} className="space-y-4">
                                <div className="auth-form-group">
                                    <label><i className="ti ti-mail"></i> Email Address</label>
                                    <input
                                        type="email"
                                        name="email"
                                        required
                                        value={paymentFormData.email}
                                        onChange={handlePaymentInputChange}
                                        placeholder="you@example.com"
                                    />
                                </div>
                                
                                <div className="auth-form-group">
                                    <label><i className="ti ti-credit-card"></i> Card Number</label>
                                    <input
                                        type="text"
                                        name="cardNumber"
                                        required
                                        maxLength="16"
                                        value={paymentFormData.cardNumber}
                                        onChange={handlePaymentInputChange}
                                        placeholder="1234 5678 9101 1121"
                                    />
                                </div>

                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                                    <div className="auth-form-group">
                                        <label><i className="ti ti-calendar"></i> Expiry Date</label>
                                        <input
                                            type="text"
                                            name="expiry"
                                            required
                                            placeholder="MM/YY"
                                            value={paymentFormData.expiry}
                                            onChange={handlePaymentInputChange}
                                        />
                                    </div>
                                    <div className="auth-form-group">
                                        <label><i className="ti ti-lock"></i> CVV</label>
                                        <input
                                            type="password"
                                            name="cvv"
                                            required
                                            maxLength="4"
                                            value={paymentFormData.cvv}
                                            onChange={handlePaymentInputChange}
                                            placeholder="123"
                                        />
                                    </div>
                                </div>

                                <button
                                    type="submit"
                                    disabled={paymentLoading}
                                    className="auth-submit-btn"
                                    style={{ marginTop: '1rem' }}
                                >
                                    <i className="ti ti-shield-lock"></i> {paymentLoading ? 'Processing...' : `Pay $${selectedPlanForPayment.numericPrice}`}
                                </button>
                            </form>
                        )}

                        {paymentStep === 'otp' && (
                            <form onSubmit={handlePaymentSubmitStep2} style={{ textAlign: 'center' }}>
                                <div style={{ marginBottom: '1rem' }}>
                                    <div style={{ background: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6', width: 56, height: 56, borderRadius: '50%', margin: '0 auto 0.75rem', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                        <i className="ti ti-shield-check" style={{ fontSize: '1.75rem' }}></i>
                                    </div>
                                    <p style={{ fontSize: '0.875rem', color: 'var(--muted-foreground)' }}>
                                        We've sent a 6-digit code to <strong>{paymentFormData.email}</strong>. Enter it below to authorize this payment.
                                    </p>
                                </div>

                                <div className="auth-form-group">
                                    <input
                                        type="text"
                                        required
                                        maxLength="6"
                                        value={paymentOtp}
                                        onChange={(e) => setPaymentOtp(e.target.value)}
                                        placeholder="------"
                                        style={{ letterSpacing: '0.3rem', textAlign: 'center', fontSize: '1.5rem', padding: '0.75rem' }}
                                    />
                                </div>

                                <button
                                    type="submit"
                                    disabled={paymentLoading || paymentOtp.length < 6}
                                    className="auth-submit-btn"
                                    style={{ background: '#22c55e', color: '#fff', marginTop: '0.5rem' }}
                                >
                                    <i className="ti ti-check"></i> {paymentLoading ? 'Verifying...' : 'Confirm & Pay'}
                                </button>
                                
                                <button
                                    type="button"
                                    onClick={() => setPaymentStep('form')}
                                    style={{ background: 'none', border: 'none', color: '#3b82f6', fontSize: '0.85rem', marginTop: '0.75rem', cursor: 'pointer', textDecoration: 'underline' }}
                                >
                                    Go Back
                                </button>
                            </form>
                        )}

                        {paymentStep === 'success' && (
                            <div style={{ textAlign: 'center', padding: '1rem 0' }}>
                                <div style={{ background: 'rgba(34, 197, 94, 0.1)', color: '#22c55e', width: 64, height: 64, borderRadius: '50%', margin: '0 auto 1rem', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                    <i className="ti ti-circle-check" style={{ fontSize: '2.5rem' }}></i>
                                </div>
                                <h3 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '0.5rem' }}>Payment Successful!</h3>
                                <p style={{ fontSize: '0.875rem', color: 'var(--muted-foreground)', marginBottom: '1rem' }}>
                                    Your transaction has been securely processed and saved.
                                </p>
                                <div style={{ background: 'var(--muted-bg-2)', padding: '0.75rem', borderRadius: '8px', fontSize: '0.85rem', marginBottom: '1.25rem' }}>
                                    Transaction ID: <br/> <span style={{ fontFamily: 'monospace', fontWeight: 'bold' }}>{transactionId}</span>
                                </div>
                                <button 
                                    onClick={() => setIsPaymentModalOpen(false)}
                                    className="auth-submit-btn"
                                >
                                    Return to Dashboard
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            )}

            {/* Navigation */}
            <nav id="navbar">
                <div className="nav-inner">
                    <a className="nav-logo" href="#home">
                        <BrandMark size={36} radius={10} />
                        MechFind
                    </a>
                    <div className="nav-links">
                        {NAV_LINKS.map((link) => (
                            <a key={link.href} href={link.href}>{link.label}</a>
                        ))}
                    </div>
                    <div className="nav-actions">
                        <button className="theme-toggle-btn" onClick={toggleTheme} aria-label="Toggle Theme">
                            <i className={`ti ${theme === 'light' ? 'ti-moon' : 'ti-sun'}`}></i>
                        </button>
                        <a className="nav-cta nav-cta-ghost" href="#app">Download App</a>
                        {currentUser ? (
                            <div className="nav-user-pill" title={currentUser.name || currentUser.email}>
                                <i className="ti ti-user-circle"></i>
                                <span style={{ overflow: 'hidden', textOverflow: 'ellipsis' }}>{currentUser.name || currentUser.email || 'User'}</span>
                                {currentSubscription && (
                                    <span style={{ fontSize: '0.7rem', background: 'var(--y)', color: '#000', padding: '2px 6px', borderRadius: '4px', fontWeight: 'bold' }}>
                                        {currentSubscription.planName || 'Active'}
                                    </span>
                                )}
                                <button className="nav-logout-btn" onClick={() => setCurrentUser(null)} aria-label="Logout">
                                    <i className="ti ti-logout"></i>
                                </button>
                            </div>
                        ) : (
                            <button className="nav-cta" onClick={() => openAuthModal('login')}>
                                <i className="ti ti-login"></i> Sign In
                            </button>
                        )}
                    </div>
                    <button className="nav-hamburger" aria-label="Open menu" onClick={() => setMobileMenuOpen((o) => !o)}>
                        <span></span><span></span><span></span>
                    </button>
                </div>
                <div className={`mobile-menu${mobileMenuOpen ? ' open' : ''}`}>
                    {NAV_LINKS.map((link) => (
                        <a key={link.href} href={link.href} onClick={closeMobileMenu}>{link.label}</a>
                    ))}
                    <a href="#app" onClick={closeMobileMenu}>Download App</a>
                    <button className="mobile-theme-btn" onClick={toggleTheme}>
                        <i className={`ti ${theme === 'light' ? 'ti-moon' : 'ti-sun'}`}></i> Toggle {theme === 'light' ? 'Dark' : 'Light'} Mode
                    </button>
                    {currentUser ? (
                        <button className="mobile-logout-btn" onClick={() => { setCurrentUser(null); closeMobileMenu(); }}>
                            <i className="ti ti-logout"></i> Logout
                        </button>
                    ) : (
                        <button className="mobile-signin-btn" onClick={() => { openAuthModal('login'); closeMobileMenu(); }}>
                            <i className="ti ti-login"></i> Sign In
                        </button>
                    )}
                </div>
            </nav>

            {/* Hero Section */}
            <section className="hero" id="home">
                <div className="hero-inner">
                    <div>
                        <div className="hero-badge">
                            <span className="hero-badge-dot"></span>
                            AI Powered Vehicle Diagnostics
                        </div>
                        <h1>Find a Mechanic,<br />Tow Truck Operator.<br /><span>Fix Your Car.</span> Fast.</h1>
                        <p>MechFind connects you to nearby mechanics or tow operators instantly. Describe your issue, our AI diagnoses it, and books the perfect professional for you.</p>
                        <div className="hero-btns">
                            <a className="btn-primary" href="#app">
                                <i className="ti ti-download"></i> Get the App
                            </a>
                            <a className="btn-liquid-glass" href="#roadmap">
                                <i className="ti ti-route"></i> See How It Works
                            </a>
                        </div>
                        <div className="avatar-row">
                            <div className="avatars">
                                <img src={avatar1} alt="Chani" />
                                <img src={avatar2} alt="Hetti" />
                                <img src={avatar3} alt="Podi" />
                                <img src={avatar4} alt="Minura" />
                            </div>
                            <p className="avatar-text">Trusted by <strong>400+ mechanics</strong></p>
                        </div>
                    </div>

                    <div className="hero-visual">
                        <div className="android-phone-frame ai-chat-preview">
                            <img src={logoMF} alt="MechFind App Logo View" className="ai-chat-interface-img" />
                        </div>
                    </div>
                </div>
            </section>

            {/* Stats */}
            <div className="stats-bar">
                <div className="stats-grid">
                    <div className="stat-item"><span className="stat-num">2.4K</span><div className="stat-label">Verified Mechanics & Tow Operators</div></div>
                    <div className="stat-item"><span className="stat-num">18K+</span><div className="stat-label">Happy Drivers</div></div>
                    <div className="stat-item"><span className="stat-num">95%</span><div className="stat-label">AI Accuracy</div></div>
                    <div className="stat-item"><span className="stat-num">4.9</span><div className="stat-label">App Rating</div></div>
                </div>
            </div>

            <div className="glass-divider"></div>

            {/* Features */}
            <section id="features">
                <div className="section-inner">
                    <div className="section-tag">Features</div>
                    <h2 className="section-title">Everything you need when<br />your car <span>breaks down</span></h2>
                    <p className="section-sub">From instant AI diagnosis to GPS based mechanic discovery MechFind handles the whole breakdown experience.</p>
                    <div className="features-grid">
                        {FEATURES.map((f) => (
                            <div className="feature-card" key={f.title}>
                                <div className="feature-icon">
                                    {f.image ? <img src={f.image} alt={f.title} /> : <i className={`ti ${f.icon}`}></i>}
                                </div>
                                <h3>{f.title}</h3>
                                <p>{f.desc}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            <div className="glass-divider"></div>

            {/* Why MechFind */}
            <section className="display-cards-section">
                <div className="section-inner">
                    <div className="display-cards-inner">
                        <div>
                            <div className="section-tag">Why MechFind</div>
                            <h2 className="section-title">Real-time help,<br /><span>every breakdown</span></h2>
                            <p className="section-sub" style={{ marginBottom: '1.5rem' }}>MechFind gives you a full picture of your vehicle's health, connects you to trusted mechanics, and handles payment — all from your pocket.</p>
                            <a className="btn-liquid-glass" href="#app">
                                <i className="ti ti-download"></i><span>Download Free</span>
                            </a>
                        </div>
                        <div className="dc-stack">
                            <div className="display-card">
                                <div className="dc-header">
                                    <div className="dc-icon-wrap gold"><img src={aiIcon} alt="AI" style={{ width: 16, height: 16 }} /></div>
                                    <span className="dc-title gold">AI Troubleshooter</span>
                                </div>
                                <p className="dc-desc">Worn brake pads — 95% Confidence</p>
                                <p className="dc-date">Est. Cost: $80 - $120 · Instant Diagnostic</p>
                            </div>
                            <div className="display-card">
                                <div className="dc-header">
                                    <div className="dc-icon-wrap green"><img src={gpsIcon} alt="GPS" style={{ width: 16, height: 16 }} /></div>
                                    <span className="dc-title green">Nearby Mechanic</span>
                                </div>
                                <p className="dc-desc">Rajan's Auto Works · 0.8 km away</p>
                                <p className="dc-date">Available now · ★★★★★ (4.9)</p>
                            </div>
                            <div className="display-card">
                                <div className="dc-header">
                                    <div className="dc-icon-wrap blue"><img src={liveViewImg} alt="Live Tracking" style={{ width: 16, height: 16 }} /></div>
                                    <span className="dc-title blue">Emergency Tow Truck</span>
                                </div>
                                <p className="dc-desc">Flatbed Towing Service dispatched</p>
                                <p className="dc-date">Live GPS tracking active · Secure App Payment</p>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <div className="glass-divider"></div>

            {/* Spotlight */}
            <section className="spotlight-section" id="spotlight">
                <div className="section-inner">
                    <div className="section-tag">Smart Platform</div>
                    <h2 className="section-title text-center mx-auto">Built with <span>precision</span></h2>
                    <p className="section-sub text-center mx-auto">Every part of MechFind is engineered so that when you're stranded on the road, help arrives — instantly.</p>
                    <div className="spotlight-grid">
                        {SPOTLIGHT.map((s) => (
                            <div className="glow-card" key={s.title}>
                                <div className="glow-card-icon"><i className={`ti ${s.icon}`}></i></div>
                                <h3>{s.title}</h3>
                                <p>{s.desc}</p>
                                <div className="glow-card-badge"><i className={`ti ${s.badge[0]}`} style={{ fontSize: '.8rem' }}></i> {s.badge[1]}</div>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            <div className="glass-divider"></div>

            {/* Roadmap */}
            <section className="roadmap-section" id="roadmap">
                <div className="section-inner text-center">
                    <div className="section-tag">{activeRoadmapCopy.tag}</div>
                    <h2 className="section-title mx-auto">{activeRoadmapCopy.title.split(' ').slice(0, -1).join(' ')} <span>{activeRoadmapCopy.title.split(' ').slice(-1)}</span></h2>
                    <p className="section-sub mx-auto">{activeRoadmapCopy.sub}</p>

                    <div className="roadmap-tabs" role="tablist" aria-label="Roadmap audience">
                        <button
                            type="button"
                            role="tab"
                            aria-selected={roadmapTab === 'driver'}
                            className={`roadmap-tab${roadmapTab === 'driver' ? ' active' : ''}`}
                            onClick={() => setRoadmapTab('driver')}
                        >
                            <i className="ti ti-steering-wheel"></i> For Drivers
                        </button>
                        <button
                            type="button"
                            role="tab"
                            aria-selected={roadmapTab === 'tow'}
                            className={`roadmap-tab${roadmapTab === 'tow' ? ' active' : ''}`}
                            onClick={() => setRoadmapTab('tow')}
                        >
                            <i className="ti ti-truck"></i> For Tow Operators
                        </button>
                    </div>

                    <div className="roadmap-container">
                        <svg className="roadmap-svg" viewBox="0 0 800 300" preserveAspectRatio="none">
                            <path className="roadmap-path" d="M 50 250 Q 200 30 400 150 T 750 80"></path>
                        </svg>
                        {activeMilestones.map((m) => (
                            <div className="milestone" key={m.label}>
                                <div className="m-dot-wrapper">
                                    <div className="m-ring"></div>
                                    <div className={`m-dot ${m.state}`}>
                                        {m.state === 'complete' && <img src={doneIcon} alt="Done" style={{ width: 12, height: 12 }} />}
                                    </div>
                                </div>
                                <div className="m-label">{m.label}</div>
                            </div>
                        ))}
                    </div>

                    <ol className="roadmap-mobile-list">
                        {activeMilestones.map((m, i) => (
                            <li key={m.label} className="roadmap-mobile-item">
                                <span className="roadmap-mobile-index">{i + 1}</span>
                                <span className="roadmap-mobile-label">{m.label.replace(/^\d+\.\s*/, '')}</span>
                            </li>
                        ))}
                    </ol>
                </div>
            </section>

            <div className="glass-divider"></div>

            {/* Pricing / Subscriptions */}
            <section id="pricing">
                <div className="section-inner text-center">
                    <div className="section-tag">Pricing & Subscriptions</div>
                    <h2 className="section-title mx-auto">Simple, <span>transparent</span> plans</h2>
                    <p className="section-sub mx-auto">No hidden fees. Select a plan to unlock higher limits and premium roadside diagnostics.</p>

                    {currentSubscription && (
                        <div className="active-sub-banner" style={{ margin: '1.5rem auto 0', maxWidth: '480px', padding: '0.85rem 1.25rem', background: 'rgba(230, 155, 0, 0.12)', border: '1px solid var(--y)', borderRadius: '14px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                            <div>
                                <small style={{ display: 'block', color: 'var(--muted-foreground)' }}>Current Subscription</small>
                                <strong>{currentSubscription.planName}</strong>
                            </div>
                            <button type="button" onClick={handleCancelSubscription} style={{ background: 'none', border: 'none', color: '#ef4444', fontWeight: 'bold', cursor: 'pointer', fontSize: '0.85rem' }}>
                                Cancel Plan
                            </button>
                        </div>
                    )}

                    <div className="pricing-wrapper">
                        {PRICING_PLANS.map((plan) => {
                            const isCurrentPlan = currentSubscription?.planName === plan.name || currentSubscription?.planId === plan.id;

                            return (
                                <div className={`price-card${plan.featured ? ' featured' : ''}`} key={plan.id}>
                                    {plan.featured && <div className="popular-badge">Most Popular</div>}
                                    <h3>{plan.name}</h3>
                                    <div className="price-amount">{plan.price} <span>{plan.period}</span></div>
                                    <ul className="price-features">
                                        {plan.features.map((f) => (
                                            <li key={f}><i className="ti ti-check"></i> {f}</li>
                                        ))}
                                    </ul>
                                    <button
                                        type="button"
                                        className={`price-btn ${isCurrentPlan ? 'active-plan-btn' : ''}`}
                                        disabled={isCurrentPlan}
                                        onClick={() => handleInitiatePlanSelection(plan)}
                                    >
                                        {isCurrentPlan ? 'Active Plan' : plan.cta}
                                    </button>
                                </div>
                            );
                        })}
                    </div>
                </div>
            </section>

            <div className="glass-divider"></div>

            {/* App Download Section */}
            <section className="app-section" id="app">
                <div className="section-inner">
                    <div className="app-inner">
                        <div>
                            <div className="section-tag">Mobile Experience</div>
                            <h2 className="section-title">Get <span>MechFind</span><br />in your pocket</h2>
                            <p className="section-sub">Download free today and get 3 free AI diagnostics every month. Search mechanics, request tow services, and track help live on the map.</p>
                            <div className="app-badges">
                                <div className="app-badge" onClick={() => handleTrackDownload('Android')}>
                                    <i className="ti ti-brand-google-play"></i>
                                    <div><small>Get it on</small><strong>Google Play</strong></div>
                                </div>
                                <div className="app-badge" onClick={() => handleTrackDownload('iOS')}>
                                    <i className="ti ti-brand-apple"></i>
                                    <div><small>Coming soon</small><strong>App Store</strong></div>
                                </div>
                            </div>
                        </div>

                        <div className="liquid-glass-cards-container">
                            <div className="liquid-glass-image-card card-1"><img src={loginImg} alt="MechFind Login Preview" /></div>
                            <div className="liquid-glass-image-card card-2"><img src={aiBotImg} alt="MechFind Logo Preview" /></div>
                            <div className="liquid-glass-image-card card-3"><img src={locationImg} alt="MechFind Location Preview" /></div>
                        </div>
                    </div>
                </div>
            </section>

            <div className="glass-divider"></div>

            {/* Reviews */}
            <section id="reviews">
                <div className="section-inner">
                    <div className="text-center" style={{ marginBottom: '2.5rem' }}>
                        <div className="section-tag">Community Hub</div>
                        <h2 className="section-title mx-auto">Live <span>Feedback</span></h2>
                        <p className="section-sub mx-auto">See what professionals are saying in real-time, or share your own experience below.</p>
                    </div>

                    <div className="features-grid reviews-grid">
                        <div className="feature-card">
                            <h3 style={{ marginBottom: '1.25rem', color: 'var(--y)' }}>Submit a Review</h3>
                            <form onSubmit={handleFeedbackSubmit}>
                                {currentUser ? (
                                    <div style={{ marginBottom: '1.0rem' }}>
                                        <label className="review-form-label">Posting as:</label>
                                        <div style={{ padding: '0.65rem 1rem', background: 'rgba(230, 155, 0, 0.08)', borderRadius: '10px', border: '1px solid rgba(230, 155, 0, 0.25)', fontWeight: '600', fontSize: '0.95rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                                            <i className="ti ti-user-check" style={{ color: 'var(--y)', fontSize: '1.1rem' }}></i>
                                            <span style={{ overflow: 'hidden', textOverflow: 'ellipsis' }}>{currentUser.name || currentUser.email}</span>
                                            <span style={{ marginLeft: 'auto', fontSize: '0.75rem', padding: '2px 8px', borderRadius: '6px', background: 'var(--y)', color: '#000', textTransform: 'uppercase', letterSpacing: '0.5px', flexShrink: 0 }}>
                                                {currentUser.userType || 'Mechanic'}
                                            </span>
                                        </div>
                                    </div>
                                ) : (
                                    <div style={{ marginBottom: '1.0rem' }}>
                                        <label className="review-form-label">I am a:</label>
                                        <select className="review-form-field" value={userType} onChange={(e) => setUserType(e.target.value)}>
                                            <option value="Mechanic">Mechanic</option>
                                            <option value="TowTruck">Tow Truck Operator</option>
                                        </select>
                                    </div>
                                )}

                                <div style={{ marginBottom: '1.0rem' }}>
                                    <label className="review-form-label">Rating:</label>
                                    <select className="review-form-field" value={rating} onChange={(e) => setRating(e.target.value)}>
                                        <option value="5">★★★★★ (5/5)</option>
                                        <option value="4">★★★★☆ (4/5)</option>
                                        <option value="3">★★★☆☆ (3/5)</option>
                                        <option value="2">★★☆☆☆ (2/5)</option>
                                        <option value="1">★☆☆☆☆ (1/5)</option>
                                    </select>
                                </div>

                                <div style={{ marginBottom: '1.25rem' }}>
                                    <label className="review-form-label">Your Feedback / Message:</label>
                                    <textarea className="review-form-field" rows="4" value={message} onChange={(e) => setMessage(e.target.value)} placeholder="Write your review or feedback here..." required></textarea>
                                </div>

                                <button type="submit" className="auth-submit-btn">
                                    <i className="ti ti-send"></i> Submit Review
                                </button>

                                {formStatus.text && (
                                    <div className={`review-status ${formStatus.type}`}>
                                        {formStatus.text}
                                    </div>
                                )}
                            </form>
                        </div>

                        <div className="feature-card" style={{ gridColumn: 'span 3' }}>
                            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '1.25rem' }}>
                                <h3 style={{ margin: 0, color: 'var(--y)' }}>Community Wall</h3>
                                <button type="button" onClick={fetchFeedbacks} style={{ background: 'none', border: 'none', color: 'var(--y)', cursor: 'pointer', fontSize: '0.85rem', fontWeight: '700', display: 'flex', alignItems: 'center', gap: '4px' }}>
                                    <i className="ti ti-refresh"></i> Refresh
                                </button>
                            </div>

                            <div className="feedback-scroll">
                                {loadingFeedbacks ? (
                                    <p style={{ color: 'var(--muted-foreground)', textAlign: 'center', padding: '2rem 0' }}>Loading community feedback...</p>
                                ) : feedbacks.length === 0 ? (
                                    <p style={{ color: 'var(--muted-foreground)', textAlign: 'center', padding: '2rem 0' }}>No reviews found yet. Be the first to share your experience!</p>
                                ) : (
                                    feedbacks.map((item, idx) => (
                                        <div className="feedback-log-item" key={item.id || idx}>
                                            <div className="feedback-log-head">
                                                <div>
                                                    <strong style={{ fontSize: '0.95rem' }}>{item.userName || `User #${item.userId || 'Anonymous'}`}</strong>
                                                    <span style={{ display: 'inline-block', marginLeft: '8px', fontSize: '0.7rem', padding: '1px 6px', borderRadius: '4px', background: 'var(--muted-bg-2)', color: 'var(--muted-foreground)' }}>{item.userType || 'Mechanic'}</span>
                                                </div>
                                                <div className="feedback-log-stars">
                                                    {'★'.repeat(item.rating || 5)}{'☆'.repeat(5 - (item.rating || 5))}
                                                </div>
                                            </div>
                                            <p className="feedback-log-msg">{item.message}</p>
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <div className="glass-divider"></div>

            {/* About / Team Section */}
            <section id="about">
                <div className="section-inner text-center">
                    <div className="section-tag">Project Team</div>
                    <h2 className="section-title mx-auto">Meet the developers behind <span>MechFind</span></h2>
                    <p className="section-sub mx-auto">Developed as part of the Higher National Diploma in Software Engineering (Batch HNDSE25.2F).</p>

                    <div className="team-grid">
                        {TEAM.map((member) => (
                            <div className="team-card" key={member.id}>
                                <div className="team-avatar">{member.initials}</div>
                                <p>{member.name}</p>
                                <span>{member.id}</span>
                            </div>
                        ))}
                    </div>

                    <div className="nibm-tag">
                        National Institute of Business Management (NIBM) — Colombo 07<br />
                        Higher National Diploma in Software Engineering (HNDSE25.2F)
                    </div>
                </div>
            </section>

            {/* Footer */}
            <footer>
                <div className="footer-inner">
                    <div className="footer-top">
                        <div className="footer-brand">
                            <a className="nav-logo" href="#home" style={{ color: '#fff' }}>
                                <BrandMark size={32} radius={8} /> MechFind
                            </a>
                            <p>Instant AI-powered vehicle diagnostics, verified roadside mechanics, and live emergency towing at your fingertips.</p>
                        </div>
                        <div className="footer-col">
                            <h4>Platform</h4>
                            <a href="#features">Features</a>
                            <a href="#spotlight">Smart Triage</a>
                            <a href="#roadmap">How it Works</a>
                            <a href="#pricing">Pricing</a>
                        </div>
                        <div className="footer-col">
                            <h4>Company</h4>
                            <a href="#about">About Us</a>
                            <a href="#reviews">Community Reviews</a>
                            <a href="#app">Mobile Apps</a>
                        </div>
                        <div className="footer-col">
                            <h4>Legal & Support</h4>
                            <a href="#home">Privacy Policy</a>
                            <a href="#home">Terms of Service</a>
                            <a href="#home">Roadside Safety</a>
                        </div>
                    </div>
                    <div className="footer-bottom">
                        <p>&copy; {new Date().getFullYear()} MechFind Inc. All rights reserved. NIBM HNDSE25.2F Project.</p>
                        <div className="footer-socials">
                            <a className="social-btn" href="https://www.instagram.com/mechf_ind?igsh=NHdzczNqbDNhZmVq&utm_source=qr" target="_blank" rel="noopener noreferrer" aria-label="Instagram"><i className="ti ti-brand-instagram"></i></a>
                            <a className="social-btn" href="https://www.facebook.com/share/1CLMSdkugp/?mibextid=wwXIfr" target="_blank" rel="noopener noreferrer" aria-label="Facebook"><i className="ti ti-brand-facebook"></i></a>
                        </div>
                    </div>
                </div>
            </footer>
        </div>
    );
}

export default App;