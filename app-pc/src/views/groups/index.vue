<script setup>
// Implementation note.
// Implementation note.
</script>

<template>
  <main class="container mx-auto px-4 py-8">
    <!-- Section -->
    <div class="mb-8 flex flex-col md:flex-row md:items-center md:justify-between">
      <div>
        <h1 class="text-3xl font-bold mb-2">
          Programming Communities
        </h1>
        <p class="text-gray-600 dark:text-gray-400">
          Join <span class="text-blue-600 dark:text-blue-400 font-medium">246</span> active communities and learn with other developers
        </p>
      </div>
      <button class="mt-4 md:mt-0 px-5 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors flex items-center space-x-2">
        <i class="fa fa-plus" />
        <span>Create Group</span>
      </button>
    </div>

    <!-- Section -->
    <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm p-5 mb-8">
      <!-- Section -->
      <div class="mb-6">
        <div class="relative">
          <input
            type="text"
            placeholder="Search groups, descriptions, or topics..."
            class="w-full pl-10 pr-4 py-3 rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
          >
          <i class="fa fa-search absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
        </div>
      </div>

      <!-- Section -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Group Category -->
        <div>
          <label class="block text-sm font-medium mb-2">Group Category</label>
          <div class="relative">
            <select class="w-full appearance-none pl-4 pr-10 py-2.5 rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all">
              <option value="">
                All Categories
              </option>
              <option value="study">
                Learning
              </option>
              <option value="competition">
                Competition Team
              </option>
              <option value="language">
                Programming Language
              </option>
              <option value="job">
                Careers
              </option>
              <option value="project">
                Project Collaboration
              </option>
              <option value="beginner">
                Beginner Help
              </option>
            </select>
            <i class="fa fa-chevron-down absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
          </div>
        </div>

        <!-- Section -->
        <div>
          <label class="block text-sm font-medium mb-2">Activity</label>
          <div class="relative">
            <select class="w-full appearance-none pl-4 pr-10 py-2.5 rounded-lg border border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all">
              <option value="">
                All Activity Levels
              </option>
              <option value="very-high">
                Very Active
              </option>
              <option value="high">
                Active
              </option>
              <option value="medium">
                Moderate
              </option>
              <option value="low">
                Low
              </option>
            </select>
            <i class="fa fa-chevron-down absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none" />
          </div>
        </div>
      </div>

      <!-- Section -->
      <div class="mt-6">
        <label class="block text-sm font-medium mb-2">Popular Topics</label>
        <div class="flex flex-wrap gap-2">
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># C++</span>
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># Python</span>
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># Competitive Programming</span>
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># Frontend Development</span>
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># Backend Development</span>
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># Machine Learning</span>
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># Interview Experience</span>
          <span class="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-sm cursor-pointer hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors"># More</span>
        </div>
      </div>

      <!-- Section -->
      <div class="mt-6 flex justify-end space-x-3">
        <button class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
          Reset
        </button>
        <button class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          Apply Filters
        </button>
      </div>
    </div>

    <!-- Recommended Groups -->
    <div class="mb-10">
      <h2 class="text-2xl font-bold mb-6">
        Recommended Groups
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Section -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300">
          <div class="relative h-40 overflow-hidden">
            <img src="https://picsum.photos/seed/programming/800/400" alt="Programming community cover" class="w-full h-full object-cover">
            <div class="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent" />
            <div class="absolute bottom-4 left-4 right-4 flex items-end justify-between">
              <div class="flex items-center space-x-3">
                <div class="w-16 h-16 rounded-full border-4 border-white dark:border-gray-800 overflow-hidden">
                  <img src="https://picsum.photos/seed/coding/200/200" alt="Programming community avatar" class="w-full h-full object-cover">
                </div>
                <div>
                  <h3 class="text-xl font-bold text-white">
                    Programming Enthusiasts
                  </h3>
                  <div class="flex items-center text-gray-200 text-sm">
                    <span class="bg-blue-600/90 text-white text-xs px-2 py-0.5 rounded mr-2">Learning</span>
                    <span><i class="fa fa-users mr-1" /> 12,540 members</span>
                  </div>
                </div>
              </div>
              <button class="px-4 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm transition-colors">
                Join
              </button>
            </div>
          </div>
          <div class="p-4">
            <p class="text-gray-600 dark:text-gray-300 text-sm mb-3">
              A community for programmers to share learning experiences, solve technical problems, and discuss industry trends.
            </p>
            <div class="flex justify-between items-center">
              <div class="flex flex-wrap gap-2">
                <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Multiple Languages</span>
                <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Tech Sharing</span>
                <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Beginner Friendly</span>
              </div>
              <span class="text-sm text-gray-500 dark:text-gray-400"><i class="fa fa-comment mr-1" /> Posts today: 248</span>
            </div>
          </div>
        </div>

        <!-- Recommended Groups2 - Competition Team -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300">
          <div class="relative h-40 overflow-hidden">
            <img src="https://picsum.photos/seed/acm/800/400" alt="ACM training team cover" class="w-full h-full object-cover">
            <div class="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent" />
            <div class="absolute bottom-4 left-4 right-4 flex items-end justify-between">
              <div class="flex items-center space-x-3">
                <div class="w-16 h-16 rounded-full border-4 border-white dark:border-gray-800 overflow-hidden">
                  <img src="https://picsum.photos/seed/compete/200/200" alt="ACM training team avatar" class="w-full h-full object-cover">
                </div>
                <div>
                  <h3 class="text-xl font-bold text-white">
                    ACM Training Team
                  </h3>
                  <div class="flex items-center text-gray-200 text-sm">
                    <span class="bg-red-600/90 text-white text-xs px-2 py-0.5 rounded mr-2">Competition Team</span>
                    <span><i class="fa fa-users mr-1" /> 328 members</span>
                  </div>
                </div>
              </div>
              <button class="px-4 py-1.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm transition-colors">
                Request to Join
              </button>
            </div>
          </div>
          <div class="p-4">
            <p class="text-gray-600 dark:text-gray-300 text-sm mb-3">
              A team focused on ACM/ICPC training, with regular mock contests and solution discussions.
            </p>
            <div class="flex justify-between items-center">
              <div class="flex flex-wrap gap-2">
                <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Algorithms</span>
                <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Contests</span>
                <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">C++</span>
              </div>
              <span class="text-sm text-gray-500 dark:text-gray-400"><i class="fa fa-comment mr-1" /> Posts today: 156</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Section -->
    <div>
      <h2 class="text-2xl font-bold mb-6">
        Active Groups
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <!-- Active Groups1 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300 transform hover:-translate-y-1">
          <div class="p-4 flex items-start space-x-3">
            <div class="w-14 h-14 rounded-full overflow-hidden flex-shrink-0">
              <img src="https://picsum.photos/seed/python/200/200" alt="Python community avatar" class="w-full h-full object-cover">
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-start">
                <h3 class="text-lg font-semibold mb-1 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                  <a href="#">Python Developer Community</a>
                </h3>
                <button class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs transition-colors">
                  Join
                </button>
              </div>
              <div class="flex items-center text-gray-500 dark:text-gray-400 text-sm mb-2">
                <span class="bg-purple-100 dark:bg-purple-900 text-purple-800 dark:text-purple-200 text-xs px-2 py-0.5 rounded mr-2">Programming Language</span>
                <span><i class="fa fa-users mr-1" /> 5,721 members</span>
              </div>
              <p class="text-gray-600 dark:text-gray-300 text-sm mb-2 line-clamp-2">
                A community for Python development, libraries, projects, and career resources.
              </p>
              <div class="flex justify-between items-center">
                <div class="flex flex-wrap gap-1">
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Python</span>
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Data Analysis</span>
                </div>
                <span class="text-xs text-gray-500 dark:text-gray-400">Active</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Active Groups2 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300 transform hover:-translate-y-1">
          <div class="p-4 flex items-start space-x-3">
            <div class="w-14 h-14 rounded-full overflow-hidden flex-shrink-0">
              <img src="https://picsum.photos/seed/frontend/200/200" alt="Frontend community avatar" class="w-full h-full object-cover">
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-start">
                <h3 class="text-lg font-semibold mb-1 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                  <a href="#">Frontend Developer Community</a>
                </h3>
                <button class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs transition-colors">
                  Join
                </button>
              </div>
              <div class="flex items-center text-gray-500 dark:text-gray-400 text-sm mb-2">
                <span class="bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 text-xs px-2 py-0.5 rounded mr-2">Learning</span>
                <span><i class="fa fa-users mr-1" /> 8,342 members</span>
              </div>
              <p class="text-gray-600 dark:text-gray-300 text-sm mb-2 line-clamp-2">
                A community for HTML, CSS, JavaScript, frontend frameworks, and practical experience.
              </p>
              <div class="flex justify-between items-center">
                <div class="flex flex-wrap gap-1">
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">JavaScript</span>
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">React</span>
                </div>
                <span class="text-xs text-gray-500 dark:text-gray-400">Very Active</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Active Groups3 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300 transform hover:-translate-y-1">
          <div class="p-4 flex items-start space-x-3">
            <div class="w-14 h-14 rounded-full overflow-hidden flex-shrink-0">
              <img src="https://picsum.photos/seed/interview/200/200" alt="Developer interview community avatar" class="w-full h-full object-cover">
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-start">
                <h3 class="text-lg font-semibold mb-1 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                  <a href="#">Developer Interview Community</a>
                </h3>
                <button class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs transition-colors">
                  Join
                </button>
              </div>
              <div class="flex items-center text-gray-500 dark:text-gray-400 text-sm mb-2">
                <span class="bg-orange-100 dark:bg-orange-900 text-orange-800 dark:text-orange-200 text-xs px-2 py-0.5 rounded mr-2">Careers</span>
                <span><i class="fa fa-users mr-1" /> 6,945 members</span>
              </div>
              <p class="text-gray-600 dark:text-gray-300 text-sm mb-2 line-clamp-2">
                Practical advice on technical interviews, algorithms, resumes, and career development.
              </p>
              <div class="flex justify-between items-center">
                <div class="flex flex-wrap gap-1">
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Interviews</span>
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Algorithms</span>
                </div>
                <span class="text-xs text-gray-500 dark:text-gray-400">Very Active</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Active Groups4 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300 transform hover:-translate-y-1">
          <div class="p-4 flex items-start space-x-3">
            <div class="w-14 h-14 rounded-full overflow-hidden flex-shrink-0">
              <img src="https://picsum.photos/seed/beginner/200/200" alt="Beginner programming community avatar" class="w-full h-full object-cover">
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-start">
                <h3 class="text-lg font-semibold mb-1 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                  <a href="#">Beginner Programming Community</a>
                </h3>
                <button class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs transition-colors">
                  Join
                </button>
              </div>
              <div class="flex items-center text-gray-500 dark:text-gray-400 text-sm mb-2">
                <span class="bg-indigo-100 dark:bg-indigo-900 text-indigo-800 dark:text-indigo-200 text-xs px-2 py-0.5 rounded mr-2">Beginner Help</span>
                <span><i class="fa fa-users mr-1" /> 4,217 members</span>
              </div>
              <p class="text-gray-600 dark:text-gray-300 text-sm mb-2 line-clamp-2">
                A friendly community that answers beginner questions and shares learning resources.
              </p>
              <div class="flex justify-between items-center">
                <div class="flex flex-wrap gap-1">
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Getting Started</span>
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Fundamentals</span>
                </div>
                <span class="text-xs text-gray-500 dark:text-gray-400">Active</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Active Groups5 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300 transform hover:-translate-y-1">
          <div class="p-4 flex items-start space-x-3">
            <div class="w-14 h-14 rounded-full overflow-hidden flex-shrink-0">
              <img src="https://picsum.photos/seed/ml/200/200" alt="Machine learning and AI group avatar" class="w-full h-full object-cover">
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-start">
                <h3 class="text-lg font-semibold mb-1 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                  <a href="#">Machine Learning & AI Group</a>
                </h3>
                <button class="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs transition-colors">
                  Join
                </button>
              </div>
              <div class="flex items-center text-gray-500 dark:text-gray-400 text-sm mb-2">
                <span class="bg-teal-100 dark:bg-teal-900 text-teal-800 dark:text-teal-200 text-xs px-2 py-0.5 rounded mr-2">Learning</span>
                <span><i class="fa fa-users mr-1" /> 3,872 members</span>
              </div>
              <p class="text-gray-600 dark:text-gray-300 text-sm mb-2 line-clamp-2">
                A community for machine learning, deep learning frameworks, AI applications, and research.
              </p>
              <div class="flex justify-between items-center">
                <div class="flex flex-wrap gap-1">
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">AI</span>
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Python</span>
                </div>
                <span class="text-xs text-gray-500 dark:text-gray-400">Active</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Active Groups6 -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-all duration-300 transform hover:-translate-y-1">
          <div class="p-4 flex items-start space-x-3">
            <div class="w-14 h-14 rounded-full overflow-hidden flex-shrink-0">
              <img src="https://picsum.photos/seed/project/200/200" alt="Open-source collaboration group avatar" class="w-full h-full object-cover">
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex justify-between items-start">
                <h3 class="text-lg font-semibold mb-1 hover:text-blue-600 dark:hover:text-blue-400 transition-colors">
                  <a href="#">Open-Source Collaboration Group</a>
                </h3>
                <button class="px-3 py-1 bg-gray-200 hover:bg-gray-300 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-800 dark:text-gray-200 rounded-lg text-xs transition-colors">
                  Request to Join
                </button>
              </div>
              <div class="flex items-center text-gray-500 dark:text-gray-400 text-sm mb-2">
                <span class="bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200 text-xs px-2 py-0.5 rounded mr-2">Project Collaboration</span>
                <span><i class="fa fa-users mr-1" /> 1,254 members</span>
              </div>
              <p class="text-gray-600 dark:text-gray-300 text-sm mb-2 line-clamp-2">
                A developer community for building open-source projects and improving collaboration skills.
              </p>
              <div class="flex justify-between items-center">
                <div class="flex flex-wrap gap-1">
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Open Source</span>
                  <span class="bg-gray-100 dark:bg-gray-700 text-xs px-2 py-0.5 rounded">Collaboration</span>
                </div>
                <span class="text-xs text-gray-500 dark:text-gray-400">Moderate</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Section -->
    <div class="mt-10 flex justify-center">
      <div class="flex items-center space-x-1">
        <button class="w-10 h-10 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed" disabled>
          <i class="fa fa-angle-left" />
        </button>
        <button class="w-10 h-10 flex items-center justify-center rounded-lg bg-blue-600 text-white">
          1
        </button>
        <button class="w-10 h-10 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
          2
        </button>
        <button class="w-10 h-10 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
          3
        </button>
        <button class="w-10 h-10 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
          4
        </button>
        <button class="w-10 h-10 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
          5
        </button>
        <button class="w-10 h-10 flex items-center justify-center rounded-lg border border-gray-200 dark:border-gray-700 hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors">
          <i class="fa fa-angle-right" />
        </button>
      </div>
    </div>
  </main>
</template>

<style scoped>
/* Component styles. */
html {
  scroll-behavior: smooth;
}

a {
  text-decoration: none;
}

/* Component styles. */
select {
  -webkit-appearance: none;
  -moz-appearance: none;
  appearance: none;
}

/* Component styles. */
.group-card:hover {
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05);
}
</style>
