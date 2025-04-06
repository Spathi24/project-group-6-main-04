<template>
<div class="home-container">
  <!-- Top Navigation -->
  <nav class="top-nav">
    <div class="user-info">
      <img src="@/assets/avatar-placeholder.png" class="avatar" />
      <div class="user-details">
        <span class="username">{{ userName }}</span>
        <span class="account-type">{{ userAccountType }}</span>
      </div>
    </div>
    <div class="nav-icons">
      <button class="icon-button">
        <MessageCircle />
      </button>
      <button class="icon-button" @click="navigateToUserSetting">
        <Settings />
      </button>
    </div>
  </nav>

  <div class="main-frame">
    <!-- Left Section: Main Menu -->
    <div class="main-menu">
      <button 
        class="menu-item" 
        :class="{ 'disabled': isPlayer }"
        @click="navigateTo('game-copy-view')"
        :title="isPlayer ? 'You need to become a Game Owner to access this functionality' : ''"
      >
        <GameController />
        <span>Your Games</span>
      </button>
      <button class="menu-item" @click="navigateTo('your-requests')">
        <ClipboardList />
        <span>Your Borrowing Requests</span>
      </button>
      <button class="menu-item" @click="navigateTo('platform-games')">
        <Library />
        <span>Games in Platform</span>
      </button>
      <div class="dropdown">
        <button class="menu-item dropdown-toggle" @click="toggleEventMenu">
          <Calendar />
          <span>Events</span>
        </button>
        <div class="dropdown-menu" v-if="showEventMenu">
          <button class="dropdown-item" @click="navigateTo('create-event')">
            <PlusCircle class="icon" />
            Create Event
          </button>
          <button class="dropdown-item" @click="navigateTo('join-events')">
            <ClipboardList class="icon" />
            Join Events
          </button>
        </div>
      </div>
    </div>

    <!-- Right Section: Upcoming Events -->
    <div class="card upcoming-events">
      <h2>Upcoming Events</h2>
      <div class="events-list">
        <div v-if="events.length > 0" class="event-item" v-for="event in events" :key="event.eventId">
          <div class="event-date">
            <span class="day">{{ event.day }}</span>
            <span class="month">{{ event.month }}</span>
          </div>
          <div class="event-details">
            <h4>{{ event.eventName }}</h4>
            <p>{{ event.maxParticipants }} participants</p>
          </div>
        </div>
        <div v-else class="no-events-message">
          {{ noEventsMessage }}
        </div>
      </div>
    </div>

    <!-- Right Section: Borrowing Requests -->
    <div class="card borrowing-requests">
      <h2>Borrowing Requests</h2>
      <div class="borrowing-list">
        <div
          v-if="borrowingRequests.length > 0"
          v-for="request in borrowingRequests"
          :key="request.id"
          class="borrowing-item"
        >
          <div class="borrowing-date">
            <span class="day">{{ request.day }}</span>
            <span class="month">{{ request.month }}</span>
          </div>
          <div class="borrowing-details">
            <h4 class="borrowing-title">{{ request.gameToBorrow }}</h4>
            <p class="borrowing-deadline">Deadline: {{ request.endDate }}</p>
          </div>
        </div>
        <div v-else class="no-events-message">
          {{ noBorrowingRequestsMessage }}
        </div>
      </div>
    </div>

    <!-- Bottom Section: Recommended Games -->
    <div class="card recommended-games">
      <h2>Recommended Games</h2>
      <div class="games-grid">
        <div class="game-card" v-for="game in recommendations" :key="game.title">
          <img :src="game.image" :alt="game.title" class="game-image">
          <div class="game-info">
            <h4 class="game-title">{{ game.title }}</h4>
            <p class="description">{{ game.description }}</p>
            <p class="borrow-message">Borrow from: {{ game.owner }}</p>
            <p class="physical-state">Physical State: {{ game.state }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
</template>
<script src="./HomePageViewScript.js"></script>
<style src="./HomePageView.css"></style>