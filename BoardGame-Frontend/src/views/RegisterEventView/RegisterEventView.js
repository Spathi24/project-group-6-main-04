import AuthService from "@/services/AuthService";
import EventService from "@/services/EventService";
import EventRegistrationService from "@/services/EventRegistrationService";
import HomeButton from '@/components/HomeButton.vue';

export default {
    name: "RegisterEventView",
    components: { HomeButton },
    data() {
        return {
            userId: AuthService.getUserID(),
            registeredEvents: [],
            availableEvents: [],
            showPastRegistered: false,
            showPastAvailable: false,
            showFullEvents: false,
            searchRegistered: "",
            searchAvailable: "",
            sortKeyRegistered: "",
            sortKeyAvailable: "",
            sortDirRegistered: 1,
            sortDirAvailable: 1,
            sortClickCountRegistered: {},
            sortClickCountAvailable: {},
        };
    },
    async mounted() {
        await this.loadEvents();
    },
    computed: {
        filteredRegisteredEvents() {
            const filtered = this.registeredEvents.filter(e =>
                (this.showPastRegistered || this.isUpcoming(e)) &&
                e.name.toLowerCase().includes(this.searchRegistered.toLowerCase())
            );
            return this.sortEvents(filtered, this.sortKeyRegistered, this.sortDirRegistered);
        },
        filteredAvailableEvents() {
            const filtered = this.availableEvents.filter(e => {
                const matchesSearch = e.name.toLowerCase().includes(this.searchAvailable.toLowerCase());
                const isUpcoming = this.showPastAvailable || this.isUpcoming(e);
                const isNotFull = this.showFullEvents || e.currentRegistrations < e.maxParticipants;
                return matchesSearch && isUpcoming && isNotFull;
            });
            return this.sortEvents(filtered, this.sortKeyAvailable, this.sortDirAvailable);
        },
    },
    methods: {
        async loadEvents() {
            try {
                const [allEventsResponse, userEventsResponse] = await Promise.all([
                    EventService.getAllEvents(),
                    EventService.getEventsByUserRegistration(this.userId),
                ]);

                const allEvents = allEventsResponse.data;
                const userEvents = userEventsResponse.data;

                const userEventIds = new Set(userEvents.map((e) => e.id));

                const formatEvent = (e) => {
                    const dateObj = new Date(e.date);
                    const timeObj = new Date(`1970-01-01T${e.time}`);
                    const startDateTime = new Date(dateObj);
                    startDateTime.setHours(timeObj.getHours(), timeObj.getMinutes());

                    return {
                        id: e.id,
                        name: e.eventName,
                        host: e.creator.name,
                        game: e.gameToPlay.title,
                        description: e.description,
                        location: e.location,
                        startTime: this.formatDateTime(e.date, e.time),
                        startDateTime,
                        currentRegistrations: e.currentRegistrations ?? 0,
                        maxParticipants: e.maxParticipant,
                    };
                };

                this.registeredEvents = userEvents.map(formatEvent);
                this.availableEvents = allEvents.filter(e => !userEventIds.has(e.id)).map(formatEvent);
            } catch (error) {
                console.error("Error loading events:", error);
            }
        },

        async joinEvent(eventId) {
            await EventRegistrationService.createRegistration({
                participantId: this.userId,
                eventId: eventId
            });
            await this.loadEvents();
        },

        async cancelEvent(eventId) {
            await EventRegistrationService.cancelRegistration(this.userId, eventId);
            await this.loadEvents();
        },

        formatDateTime(date, time) {
            const d = new Date(date);
            const t = new Date(`1970-01-01T${time}`);
            const hours = t.getHours().toString().padStart(2, "0");
            const minutes = t.getMinutes().toString().padStart(2, "0");
            return `${d.toDateString()} at ${hours}:${minutes}`;
        },

        isUpcoming(event) {
            return event.startDateTime > new Date();
        },

        sortEvents(events, key, dir) {
            if (!key) return events;
            return [...events].sort((a, b) => {
                const valA = a[key]?.toString().toLowerCase() ?? "";
                const valB = b[key]?.toString().toLowerCase() ?? "";
                return valA.localeCompare(valB) * dir;
            });
        },

        toggleSort(table, key) {
            if (table === "registered") {
                const count = (this.sortClickCountRegistered[key] || 0) + 1;
                this.sortClickCountRegistered[key] = count;

                if (count === 1) {
                    this.sortKeyRegistered = key;
                    this.sortDirRegistered = 1;
                } else if (count === 2) {
                    this.sortDirRegistered = -1;
                } else {
                    this.sortKeyRegistered = "";
                    this.sortClickCountRegistered[key] = 0;
                }
            } else {
                const count = (this.sortClickCountAvailable[key] || 0) + 1;
                this.sortClickCountAvailable[key] = count;

                if (count === 1) {
                    this.sortKeyAvailable = key;
                    this.sortDirAvailable = 1;
                } else if (count === 2) {
                    this.sortDirAvailable = -1;
                } else {
                    this.sortKeyAvailable = "";
                    this.sortClickCountAvailable[key] = 0;
                }
            }
        },
    },
};
