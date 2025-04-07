<template>
  <div class="join-events-container">
    <div class="section-header">
      <h2>Events You Registered For</h2>
      <div class="header-controls">
        <label class="toggle-past">
          <input type="checkbox" v-model="showPastRegistered" />
          Show past events
        </label>
        <input type="text" v-model="searchRegistered" placeholder="Search registered events..." class="search-bar" />
      </div>
    </div>
    <div class="event-table card">
      <table>
        <thead>
        <tr>
          <th @click="toggleSort('registered', 'name')">Event Name <span v-if="sortKeyRegistered === 'name'">{{ sortDirRegistered === 1 ? '▲' : '▼' }}</span></th>
          <th @click="toggleSort('registered', 'host')">Host <span v-if="sortKeyRegistered === 'host'">{{ sortDirRegistered === 1 ? '▲' : '▼' }}</span></th>
          <th @click="toggleSort('registered', 'game')">Game <span v-if="sortKeyRegistered === 'game'">{{ sortDirRegistered === 1 ? '▲' : '▼' }}</span></th>
          <th @click="toggleSort('registered', 'description')">Description <span v-if="sortKeyRegistered === 'description'">{{ sortDirRegistered === 1 ? '▲' : '▼' }}</span></th>
          <th @click="toggleSort('registered', 'location')">Location <span v-if="sortKeyRegistered === 'location'">{{ sortDirRegistered === 1 ? '▲' : '▼' }}</span></th>
          <th @click="toggleSort('registered', 'startTime')">Start Time <span v-if="sortKeyRegistered === 'startTime'">{{ sortDirRegistered === 1 ? '▲' : '▼' }}</span></th>
          <th>Participants</th>
          <th>Action</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="filteredRegisteredEvents.length === 0">
          <td colspan="8" class="no-data">You have not joined any events yet.</td>
        </tr>
        <tr v-for="event in filteredRegisteredEvents" :key="'registered-' + event.id">
          <td>{{ event.name }}</td>
          <td>{{ event.host }}</td>
          <td>{{ event.game }}</td>
          <td>{{ event.description }}</td>
          <td>{{ event.location }}</td>
          <td>{{ event.startTime }}</td>
          <td>{{ event.currentRegistrations }} / {{ event.maxParticipants }}</td>
          <td>
            <button class="action-button cancel" @click="cancelEvent(event.id)">Cancel</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <div class="section-header">
      <h2>All Available Events</h2>
      <div class="header-controls">
        <label class="toggle-past">
          <input type="checkbox" v-model="showFullEvents" />
          Show full events
        </label>
        <label class="toggle-past">
          <input type="checkbox" v-model="showPastAvailable" />
          Show past events
        </label>
        <input type="text" v-model="searchAvailable" placeholder="Search available events..." class="search-bar" />
      </div>
    </div>
    <div class="event-table card">
      <table>
        <thead>
        <tr>
          <th @click="toggleSort('available', 'name')">Event Name <span v-if="sortKeyAvailable === 'name'">{{ sortDirAvailable === 1 ? '▲' : '▼' }}</span></th>
          <th @click="toggleSort('available', 'host')">Host <span v-if="sortKeyAvailable === 'host'">{{ sortDirAvailable === 1 ? '▲' : '▼' }}</span></th>
          <th @click="toggleSort('available', 'game')">Game <span v-if="sortKeyAvailable === 'game'">{{ sortDirAvailable === 1 ? '▲' : '▼' }}</span></th>
          <th @click="toggleSort('available', 'description')">Description <span v-if="sortKeyAvailable === 'description'">{{ sortDirAvailable === 1 ? '▲' : '▼' }}</span></th>
          <th @click="toggleSort('available', 'location')">Location <span v-if="sortKeyAvailable === 'location'">{{ sortDirAvailable === 1 ? '▲' : '▼' }}</span></th>
          <th @click="toggleSort('available', 'startTime')">Start Time <span v-if="sortKeyAvailable === 'startTime'">{{ sortDirAvailable === 1 ? '▲' : '▼' }}</span></th>
          <th>Participants</th>
          <th>Action</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="filteredAvailableEvents.length === 0">
          <td colspan="8" class="no-data">No available events to join.</td>
        </tr>
        <tr v-for="event in filteredAvailableEvents" :key="'available-' + event.id">
          <td>{{ event.name }}</td>
          <td>{{ event.host }}</td>
          <td>{{ event.game }}</td>
          <td>{{ event.description }}</td>
          <td>{{ event.location }}</td>
          <td>{{ event.startTime }}</td>
          <td>{{ event.currentRegistrations }} / {{ event.maxParticipants }}</td>
          <td>
            <button class="action-button join" @click="joinEvent(event.id)">Join</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <HomeButton />
  </div>
</template>

<script src="./RegisterEventView.js"></script>
<style scoped src="./RegisterEventViewStyles.css"></style>