<template>
  <div class="container">
    <div class="content">
      <h1 class="title">Games you own</h1>

      <div class="controls">
        <input type="text" v-model="searchQuery" placeholder="search your games" @input="filterGames" />
        <div class="button-group">
          <button class="add-button" @click="addGame">
            <Size16 class="size-16" />
            Add Game
          </button>
          <button class="delete-button" @click="deleteSelected" :disabled="selectedGames.length === 0">
            <Trash2 class="size-16" />
            Delete Games
          </button>
        </div>
      </div>

      <div v-if="loading" class="loading-spinner">
        <div class="spinner"></div>
        <p>Loading your games...</p>
      </div>

      <div v-else-if="error" class="error-message">
        <p>{{ error }}</p>
        <div class="retry-button-container">
          <button @click="fetchGames" class="btn-retry">Try Again</button>
        </div>
      </div>

      <div v-else-if="games.length === 0" class="no-games">
        <p>You don't have any games yet. Add your first game to get started!</p>
        <button class="add-button" @click="addGame">
          <Size16 class="size-16" />
          Add Game
        </button>
      </div>

      <div v-else class="table">
        <div class="table-content">
          <table>
            <thead>
              <tr>
                <th><Cell type="checkbox" type2="checkbox-only" @click="toggleSelectAll" /></th>
                <th>Name</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="game in filteredGames" :key="game.title">
                <td>
                  <Cell 
                    type="checkbox" 
                    :checked="isSelected(game.title)" 
                    @click="() => toggleSelect(game.title)" 
                  />
                </td>
                <td>{{ game.title }}</td>
                <td>
                  <div class="status-wrapper">
                    <StatusCircle :status="capitalizedStatus(game.status)" />
                    {{ capitalizedStatus(game.status) }}
                  </div>
                </td>
                <td class="actions">
                  <Edit 
                    :class="['icon', { 'icon-disabled': isBorrowed(game.status) }]" 
                    @click="!isBorrowed(game.status) && editGame(game.title)" 
                    :title="isBorrowed(game.status) ? 'Cannot edit borrowed games' : 'Edit game'"
                  />
                  <Trash2 
                    :class="['icon', { 'icon-disabled': isBorrowed(game.status) }]" 
                    @click="!isBorrowed(game.status) && confirmDeleteGame(game.title)" 
                    :title="isBorrowed(game.status) ? 'Cannot delete borrowed games' : 'Delete game'"
                  />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Confirmation Dialog for Delete -->
      <div v-if="showDeleteConfirm" class="delete-confirm-overlay">
        <div class="delete-confirm-dialog">
          <h3>Confirm Deletion</h3>
          <p>Are you sure you want to delete {{ deleteType === 'single' ? 'this game' : 'these games' }}?</p>
          <p v-if="deleteType === 'single'">This action cannot be undone.</p>
          <p v-else>You are about to delete {{ selectedGames.length }} games. This action cannot be undone.</p>
          <div class="button-container">
            <button class="btn-cancel" @click="cancelDelete">Cancel</button>
            <button class="btn-confirm" @click="proceedWithDelete">Yes, Delete</button>
          </div>
        </div>
      </div>
    </div>
    <HomeButton />
    <router-view />
  </div>
</template>

<script src="./GameCopyView.js"></script>

<style src="./GameCopyViewStyles.css"></style>