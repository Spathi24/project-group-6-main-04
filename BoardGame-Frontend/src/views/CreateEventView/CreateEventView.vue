<template>
 <div class="game-overlay" @click.self="closePopup">
   <div class="game-popup">
     <h1>Create Event</h1>


     <div v-if="loading" class="loading-spinner">
       <div class="spinner"></div>
       <p>Loading event details...</p>
     </div>


     <div v-else-if="error" class="error-message">
       <p>{{ error }}</p>
       <div class="retry-button-container">
         <button @click="fetchEventDetails" class="btn-retry">Try Again</button>
       </div>
     </div>


     <template v-else>
       <form @submit.prevent="saveEvent" class="form-container">
         <div class="form-group">
           <label for="event-name">Event Name</label>
           <input id="event-name" type="text" v-model="event.name" required />
         </div>


         <div class="form-group">
           <label for="event-description">Event Description</label>
           <textarea
             id="event-description"
             v-model="event.description"
             placeholder="Describe the event"
             required
           ></textarea>
         </div>


         <div class="form-group">
           <label for="event-date">Event Date</label>
           <input
             id="event-date"
             type="date"
             v-model="event.date"
             required
           />
         </div>


         <div class="form-group">
           <label for="event-location">Event Location</label>
           <input
             id="event-location"
             type="text"
             v-model="event.location"
             required
           />
         </div>


         <div class="button-container">
           <button type="submit" class="btn btn-success" :disabled="submitting">
             <span v-if="submitting">Creating...</span>
             <span v-else>Create Event</span>
           </button>
           <button type="button" class="btn btn-danger" @click="closePopup" :disabled="submitting">Cancel</button>
         </div>
       </form>


       <!-- Success Message -->
       <div v-if="showSuccess" class="success-message">
         <p>Event created successfully!</p>
         <p>Redirecting to your event list...</p>
       </div>
     </template>
   </div>
   <HomeButton :showText="false" />
 </div>
</template>


<script>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import HomeButton from '@/components/HomeButton.vue';
import EventService from '@/services/EventService';


export default {
 name: 'CreateEventView',
 components: {
   HomeButton
 },
 props: {
   userId: {
     type: [Number, String],
     required: true
   },
 },
 setup(props) {
   const route = useRoute();
   const router = useRouter();
   const event = ref({ name: '', description: '', date: '', location: '' });
   const loading = ref(true);
   const error = ref(null);
   const submitting = ref(false);
   const showSuccess = ref(false);


   const userId = computed(() => props.userId || route.params.userId);


   const fetchEventDetails = async () => {
     loading.value = true;
     error.value = null;


     try {
       // Example of fetching event details if needed
       // const eventData = await EventService.getEventById(userId.value);
       // Object.assign(event.value, eventData);
     } catch (err) {
       error.value = err.message || "Failed to load event details. Please try again.";
       console.error("Error fetching event details:", err);
     } finally {
       loading.value = false;
     }
   };


   onMounted(fetchEventDetails);


   const saveEvent = async () => {
     submitting.value = true;

     try {
       await EventService.createEvent(userId.value, event.value);


       showSuccess.value = true;
       setTimeout(() => {
         router.push({ name: 'event-list-view', params: { userId: userId.value } });
       }, 1500);
     } catch (err) {
       error.value = err.message || "Failed to create the event. Please try again.";
       console.error("Error creating event:", err);
       submitting.value = false;
     }
   };


   const closePopup = () => {
     router.push({ name: 'event-list-view', params: { userId: userId.value } });
   };


   return {
     userId,
     event,
     loading,
     error,
     submitting,
     showSuccess,
     fetchEventDetails,
     saveEvent,
     closePopup
   };
 }
};
</script>


<style src="./CreateEventViewStyles.css"></style>


<style scoped>
.loading-spinner {
 display: flex;
 flex-direction: column;
 align-items: center;
 justify-content: center;
 padding: 2rem;
}


.spinner {
 border: 4px solid rgba(255, 255, 255, 0.3);
 border-radius: 50%;
 border-top: 4px solid #ff6f61;
 width: 30px;
 height: 30px;
 animation: spin 1s linear infinite;
 margin-bottom: 1rem;
}


@keyframes spin {
 0% { transform: rotate(0deg); }
 100% { transform: rotate(360deg); }
}


.error-message {
 color: #ff6f61;
 padding: 1rem 0;
 text-align: center;
 font-family: 'Inter', sans-serif;
}


.retry-button-container {
 display: flex;
 justify-content: center;
 margin-top: 1rem;
}


.btn-retry {
 background-color: #ff6f61;
 color: white;
 border: none;
 padding: 0.5rem 1rem;
 border-radius: 4px;
 cursor: pointer;
 font-family: 'Inter', sans-serif;
 font-weight: 500;
 transition: background-color 0.3s, transform 0.2s;
}


.btn-retry:hover {
 background-color: #e65a50;
 transform: translateY(-2px);
}


.success-message {
 color: #4CAF50;
 text-align: center;
 padding: 2rem;
}
</style>

