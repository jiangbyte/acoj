<template>
  <section class="mb-12 rounded-2xl overflow-hidden shadow-lg">
    <div class="my-section">
      <div
        class="carousel-item"
      >
        <img
          class="carousel-img"
        >
        <div class="carousel-caption">
          <div class="caption-title">
            <n-skeleton class="w-50 h-7 mb-2" />
          </div>
          <div class="caption-subtitle">
            <n-skeleton text :repeat="1" class="w-35" />
          </div>
          <n-skeleton class="h-8 w-20" />
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.my-section {
  border-radius: 0.5rem;
  overflow: hidden;
  position: relative;
}

.carousel-item {
  position: relative;
  height: 320px;
}

@media (max-width: 768px) {
  .carousel-item {
    height: 250px;
  }

  .carousel-caption {
    padding: 40px 20px;
  }
}

@media (max-width: 480px) {
  .carousel-item {
    height: 250px;
  }

  .carousel-caption {
    padding: 30px 15px;
  }
}

.carousel-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-caption {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px 10px 40px 40px;
  z-index: 1;
}

.caption-title {
  margin: 0;
}

.caption-subtitle {
  font-size: 1.0rem;
  margin: 4px 0 8px;
  max-width: 600px;
}
</style>
