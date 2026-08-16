import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { Draggable } from 'gsap/Draggable';

gsap.registerPlugin(ScrollTrigger, Draggable);
/**
 * Fades + lifts every direct child of a section into view on scroll.
 * One trigger per section keeps this to a single ScrollTrigger per
 * region instead of one per card, which is what the DOM had before.
 */
export function initScrollReveals() {
    const groups = document.querySelectorAll('[data-reveal-group]');
    const triggers = [];

    groups.forEach((group) => {
        const items = group.querySelectorAll('[data-reveal]');
        if (!items.length) return;

        gsap.set(items, { opacity: 0, y: 32 });

        const tween = gsap.to(items, {
            opacity: 1,
            y: 0,
            duration: 0.7,
            ease: 'power3.out',
            stagger: 0.08,
            scrollTrigger: {
                trigger: group,
                start: 'top 82%',
                once: true,
            },
        });

        triggers.push(tween.scrollTrigger);
    });

    return () => triggers.forEach((t) => t && t.kill());
}

/**
 * Hero entrance — a single orchestrated timeline rather than scattered
 * per-element animations, per the "one orchestrated moment" principle.
 */
export function initHeroIntro() {
    const badge = document.querySelector('.hero-badge');
    const heading = document.querySelector('.hero h1');
    const copy = document.querySelector('.hero p');
    const btns = document.querySelector('.hero-btns');
    const avatars = document.querySelector('.avatar-row');
    const visual = document.querySelector('.hero-visual');

    if (!heading) return () => {};

    const tl = gsap.timeline({ defaults: { ease: 'power3.out' } });
    tl.set([badge, heading, copy, btns, avatars, visual].filter(Boolean), { opacity: 0 })
        .to(badge, { opacity: 1, y: 0, duration: 0.5 }, 0.1)
        .from(heading, { y: 28, duration: 0.7 }, 0.15)
        .to(heading, { opacity: 1 }, 0.15)
        .from(copy, { y: 20, duration: 0.6 }, 0.3)
        .to(copy, { opacity: 1 }, 0.3)
        .from(btns, { y: 16, duration: 0.5 }, 0.42)
        .to(btns, { opacity: 1 }, 0.42)
        .from(avatars, { y: 12, duration: 0.5 }, 0.52)
        .to(avatars, { opacity: 1 }, 0.52)
        .from(visual, { y: 24, scale: 0.96, duration: 0.8 }, 0.25)
        .to(visual, { opacity: 1 }, 0.25);

    return () => tl.kill();
}

/**
 * Feature / glow card hover: lift + border-color pulse + icon nudge.
 * Replaces the old per-card pointermove listeners that wrote raw
 * --mx/--my/--gx/--gy custom properties by hand.
 */
export function initCardHovers() {
    const cards = document.querySelectorAll('.feature-card, .glow-card, .team-card, .app-badge, .display-card');
    const cleanups = [];

    cards.forEach((card) => {
        const icon = card.querySelector('.feature-icon, .glow-card-icon, .team-avatar, i');
        const quickY = gsap.quickTo(card, 'y', { duration: 0.35, ease: 'power3.out' });
        const quickScale = gsap.quickTo(card, 'scale', { duration: 0.35, ease: 'power3.out' });

        const onEnter = () => {
            quickY(-6);
            quickScale(1.015);
            if (icon) gsap.to(icon, { scale: 1.1, duration: 0.35, ease: 'back.out(2)' });
        };
        const onLeave = () => {
            quickY(0);
            quickScale(1);
            if (icon) gsap.to(icon, { scale: 1, duration: 0.3, ease: 'power2.out' });
        };

        card.addEventListener('mouseenter', onEnter);
        card.addEventListener('mouseleave', onLeave);
        cleanups.push(() => {
            card.removeEventListener('mouseenter', onEnter);
            card.removeEventListener('mouseleave', onLeave);
        });
    });

    return () => cleanups.forEach((fn) => fn());
}

/**
 * Pricing section: cards behave as a draggable, inertial carousel.
 * Bounds are clamped to the wrapper so the set can't be flung away
 * entirely; a soft snap-back communicates the edges.
 */
export function initPricingDrag() {
    const wrapper = document.querySelector('.pricing-wrapper');
    if (!wrapper) return () => {};

    const cards = wrapper.querySelectorAll('.price-card');
    const cardWidth = 320 + 32;
    const maxDrag = Math.max(0, (cards.length * cardWidth) - wrapper.offsetWidth + 64);

    const draggables = Draggable.create(wrapper, {
        type: 'x',
        bounds: { minX: -maxDrag, maxX: 0 },
        edgeResistance: 0.75,
        cursor: 'grab',
        activeCursor: 'grabbing',
    });

    return () => draggables.forEach((d) => d.kill());
}

/**
 * Roadmap SVG path — animates the dashed connector drawing itself in
 * as the section enters view.
 */
export function initRoadmapDraw() {
    const path = document.querySelector('.roadmap-path');
    if (!path) return () => {};

    const length = path.getTotalLength();
    gsap.set(path, { strokeDasharray: length, strokeDashoffset: length, opacity: 1 });

    const tween = gsap.to(path, {
        strokeDashoffset: 0,
        duration: 1.6,
        ease: 'power2.inOut',
        scrollTrigger: {
            trigger: '.roadmap-container',
            start: 'top 75%',
            once: true,
        },
    });

    return () => tween.scrollTrigger && tween.scrollTrigger.kill();
}

/**
 * Boots every animation module and returns one combined cleanup for
 * the component's useEffect return.
 */
export function initAllAnimations() {
    const cleanups = [
        initHeroIntro(),
        initScrollReveals(),
        initCardHovers(),
        initPricingDrag(),
        initRoadmapDraw(),
    ];

    ScrollTrigger.refresh();

    return () => {
        cleanups.forEach((fn) => fn && fn());
        ScrollTrigger.getAll().forEach((t) => t.kill());
    };
}