"use client";

import CoverflowCarousel from "./coverflow-carousel";

const R2 = "https://pub-940ccf6255b54fa799a9b01050e6c227.r2.dev/stock-images";
const UNSPLASH = (id: string) =>
  `https://images.unsplash.com/photo-${id}?w=640&h=640&fit=crop&q=70&auto=format`;

const SLIDES = [
  {
    src: `${R2}/767d99bb371a54d0d36751e8cecae43c.jpg`,
    alt: "Diver silhouetted inside a sunset seascape shaped like a profile",
    title: "Tidewater",
    subtitle: "Long Player",
  },
  {
    src: `${R2}/821d815affa6496c39cbdeeec7a84603.jpg`,
    alt: "Double-exposure portrait blended with a city skyline at dusk",
    title: "Nightshift",
    subtitle: "Long Player",
  },
  {
    src: UNSPLASH("1470071459604-3b5ec3a7fe05"),
    alt: "Fog rolling through a forested valley at first light",
    title: "Low Country",
    subtitle: "Long Player",
  },
];

export default function DemoOne() {
  return (
    <div className="w-full overflow-hidden bg-background py-6">
      <CoverflowCarousel slides={SLIDES} showCaption showNavigation showPagination />
    </div>
  );
}
