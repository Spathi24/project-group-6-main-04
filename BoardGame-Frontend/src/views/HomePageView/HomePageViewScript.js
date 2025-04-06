import MessageCircle from "@/components/icons/MessageCircle.vue";
import Settings from "@/components/icons/Settings.vue";
import GameController from "@/components/icons/GameController.vue";
import PlusCircle from "@/components/icons/PlusCircle.vue";
import ClipboardList from "@/components/icons/ClipboardList.vue";
import Library from "@/components/icons/Library.vue";
import Calendar from "@/components/icons/Calendar.vue";
import UserAccountService from "@/services/UserAccountService";
import EventService from "@/services/EventService";
import BorrowRequestService from "@/services/BorrowRequestService";
import GameCopyService from "@/services/GameCopyService";
import EventRegistrationService from "@/services/EventRegistrationService";

// Import both images using ES module syntax
import gamePlaceholderImage1 from "@/assets/game-placeholder.jpg";
import gamePlaceholderImage2 from "@/assets/game-placeholder2.jpg";

import axios from "axios";

const axiosClient = axios.create({
  baseURL: "http://localhost:8080"
});

export default {
  name: "HomePageView",
  components: {
    MessageCircle,
    Settings,
    GameController,
    PlusCircle,
    ClipboardList,
    Library,
    Calendar,
  },
  data() {
    return {
      // Mock user ID for testing purposes
      mockUserId: 4102,
      showEventMenu: false,
      user: {
        name: "Donald Trump",
        accountType: "GAMEOWNER",
      },
      events: [],
      borrowingRequests: [],
      recommendations: [],
      noEventsMessage: "", // New property for empty events message
      noBorrowingRequestsMessage: "", // New property for empty borrowing requests message
      userName: null,
      userAccountType: null,
    };
  },
  async created() {
    try {
      const storedUserAccountID = localStorage.getItem("userAccountID");
      const response = await axiosClient.get("/UserAccount/"+storedUserAccountID);
      const u = response.data;
      this.userName = u.name;
      this.userAccountType = u.accountType;
    } catch (e) {
      alert("There is error");
    }
  },
  computed: {
    isPlayer() {
      // Check if user's account type is PLAYER
      return this.user.accountType === "Player";
    },
  },
  mounted() {
    this.fetchUserData();
    this.fetchUserRegisteredEvents();
    this.fetchBorrowingRequests();
    this.fetchRecommendations();
  },
  methods: {
    navigateTo(route) {
      // Don't navigate to game-copy-view if the user is a player
      if (route === "game-copy-view" && this.isPlayer) {
        return; // Just return without navigating
      }

      // Pass userId to game-related routes
      if (route === "game-copy-view") {
        this.$router.push({ name: route, params: { userId: this.mockUserId } });
      } else if (route === "add-game") {
        this.$router.push({
          name: "AddGame",
          params: { userId: this.mockUserId },
        });
      } else if (route === "create-event") {
        this.$router.push({
          name: "CreateEventView",
          params: { userId: this.mockUserId },
        });
      } else {
        this.$router.push({ name: route });
      }
    },
    toggleEventMenu() {
      this.showEventMenu = !this.showEventMenu;
    },
    fetchUserData() {
      UserAccountService.getUser(this.mockUserId)
        .then((response) => {
          const userData = response.data;
          this.user = {
            name: userData.name,
            accountType: this.formatAccountType(userData.accountType),
          };
        })
        .catch((error) => {
          console.error("Error fetching user data:", error);
        });
    },
    fetchUserRegisteredEvents() {
      // Reset the message first
      this.noEventsMessage = "";

      // First get all event registrations for the current user
      EventRegistrationService.getAllRegistrationsByUser(this.mockUserId)
        .then((registrationsResponse) => {
          // Extract event IDs from registrations
          const userEventIds = registrationsResponse.data.map(
            (reg) => reg.eventId
          );

          if (userEventIds.length === 0) {
            this.events = [];
            this.noEventsMessage =
              "You haven't registered for any events yet. Join an event to get started!";
            return;
          }

          // Then get all events
          EventService.getAllEvents()
            .then((eventsResponse) => {
              // Filter events to only include those the user is registered for
              const userEvents = eventsResponse.data.filter((event) =>
                userEventIds.includes(event.creatorId + "-" + event.eventName)
              );

              // Check if there are any matching events after filtering
              if (userEvents.length === 0) {
                this.events = [];
                this.noEventsMessage =
                  "No upcoming events found. Join an event to get started!";
                return;
              }

              // Format the events for display
              this.events = userEvents.map((event) => {
                const eventDate = new Date(event.eventDate);
                return {
                  eventId: event.creatorId + "-" + event.eventName,
                  eventName: event.eventName,
                  maxParticipants: event.maxParticipants,
                  eventDate: event.eventDate,
                  day: eventDate.getDate(),
                  month: this.getMonthName(eventDate.getMonth()),
                };
              });
            })
            .catch((error) => {
              console.error("Error fetching events:", error);
              this.noEventsMessage =
                "Error loading events. Please try again later.";
            });
        })
        .catch((error) => {
          console.error("Error fetching user event registrations:", error);
          this.noEventsMessage =
            "Error loading event registrations. Please try again later.";
        });
    },
    fetchBorrowingRequests() {
      // Reset the message first
      this.noBorrowingRequestsMessage = "";

      BorrowRequestService.getUserRequests(this.mockUserId)
        .then((response) => {
          if (!response.data || response.data.length === 0) {
            this.borrowingRequests = [];
            this.noBorrowingRequestsMessage =
              "You don't have any borrowing requests yet. Borrow a game to get started!";
            return;
          }

          this.borrowingRequests = response.data.map((request) => {
            const startDate = new Date(request.startDate);
            const endDate = new Date(request.endDate);

            return {
              id: request.id,
              gameToBorrow: request.gameTitle,
              startDate: request.startDate,
              endDate: this.formatDate(endDate),
              day: startDate.getDate(),
              month: this.getMonthName(startDate.getMonth()),
            };
          });
        })
        .catch((error) => {
          console.error("Error fetching borrowing requests:", error);
          this.noBorrowingRequestsMessage =
            "Error loading borrowing requests. Please try again later.";
        });
    },
    fetchRecommendations() {
      GameCopyService.getAllGameCopies()
        .then((response) => {
          const allGames = response.data;
          // Filter out games owned by the current user
          const otherUsersGames = allGames.filter(
            (game) => game.owner !== this.mockUserId
          );

          // Get two random games for recommendations (or fewer if not enough games)
          const shuffled = [...otherUsersGames].sort(() => 0.5 - Math.random());
          const selectedGames = shuffled.slice(0, Math.min(2, shuffled.length));

          // Create an array of both placeholder images
          const placeholderImages = [
            gamePlaceholderImage1,
            gamePlaceholderImage2,
          ];

          this.recommendations = selectedGames.map((game) => ({
            title: game.title,
            description:
              game.originalDescription || "AI powered suggestion for you!",
            owner: game.ownerName,
            state: game.description,
            // Randomly select one of the placeholder images
            image:
              placeholderImages[
                Math.floor(Math.random() * placeholderImages.length)
              ],
          }));
        })
        .catch((error) => {
          console.error("Error fetching game recommendations:", error);
        });
    },
    formatDate(date) {
      const options = { year: "numeric", month: "short", day: "numeric" };
      return date.toLocaleDateString("en-US", options);
    },
    getMonthName(monthIndex) {
      const months = [
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec",
      ];
      return months[monthIndex];
    },
    formatAccountType(accountType) {
      if (!accountType) return "";

      switch (accountType) {
        case "GAMEOWNER":
          return "Game Owner";
        case "PLAYER":
          return "Player";
        default:
          return accountType; // Return as-is for unknown types
      }
    },
    navigateToUserSetting(){
      this.$router.push("/userSetting");
    }
  },
};
