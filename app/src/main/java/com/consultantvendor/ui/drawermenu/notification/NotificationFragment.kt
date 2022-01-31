package com.consultantvendor.ui.drawermenu.notification

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Notification
import com.consultantvendor.data.network.ApiKeys.AFTER
import com.consultantvendor.data.network.ApiKeys.PER_PAGE
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.PER_PAGE_LOAD
import com.consultantvendor.data.network.PushType
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.ActivityListingToolbarBinding
import com.consultantvendor.ui.chat.ChatViewModel
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.ui.loginSignUp.SignUpActivity
import com.consultantvendor.ui.loginSignUp.document.DocumentsFragment
import com.consultantvendor.ui.loginSignUp.subcategory.SubCategoryFragment
import com.consultantvendor.utils.*
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.item_no_data.view.*
import javax.inject.Inject

class NotificationFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: ActivityListingToolbarBinding

    private var rootView: View? = null

    private lateinit var viewModel: ChatViewModel

    private var items = ArrayList<Notification>()

    private lateinit var adapter: NotificationAdapter

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.activity_listing_toolbar, container, false)
            rootView = binding.root

            initialise()
            setAdapter()
            listeners()
            bindObservers()
            hitApi(true)
        }
        return rootView
    }

    private fun initialise() {
        binding.tvTitle.text = getString(R.string.notification)

        viewModel = ViewModelProvider(this, viewModelFactory)[ChatViewModel::class.java]

        binding.clNoData.ivNoData.setImageResource(R.drawable.ic_notifications_empty)
        binding.clNoData.tvNoData.text = getString(R.string.no_notification)
        binding.clNoData.tvNoDataDesc.text = getString(R.string.no_notification_desc)

    }

    private fun setAdapter() {
        adapter = NotificationAdapter(this,items)
        binding.rvListing.adapter = adapter
        binding.rvListing.itemAnimator = null
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.swipeRefresh.setOnRefreshListener {
            hitApi(true)
        }


        binding.rvListing.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.rvListing.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount - 1
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoadingMoreItems && !isLastPage && lastVisibleItemPosition >= totalItemCount) {
                    isLoadingMoreItems = true
                    hitApi(false)
                }
            }
        })
    }

    private fun hitApi(firstHit: Boolean) {
        if (isConnectedToInternet(requireContext(), true)) {
            if (firstHit) {
                isFirstPage = true
                isLastPage = false
            }

            val hashMap = HashMap<String, String>()
            if (!isFirstPage && items.isNotEmpty())
                hashMap[AFTER] = items[items.size - 1].id ?: ""

            hashMap[PER_PAGE] = PER_PAGE_LOAD.toString()

            viewModel.notifications(hashMap)
        } else
            binding.swipeRefresh.isRefreshing = false
    }

    private fun bindObservers() {
        viewModel.notifications.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false

                    isLoadingMoreItems = false

                    val tempList = it.data?.notifications ?: emptyList()
                    if (isFirstPage) {
                        isFirstPage = false
                        items.clear()
                        items.addAll(tempList)

                        adapter.notifyDataSetChanged()
                    } else {
                        val oldSize = items.size
                        items.addAll(tempList)

                        adapter.notifyItemRangeInserted(oldSize, items.size)
                    }

                    isLastPage = tempList.size < PER_PAGE_LOAD
                    adapter.setAllItemsLoaded(isLastPage)

                    binding.clNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    isLoadingMoreItems = false
                    adapter.setAllItemsLoaded(true)

                    binding.swipeRefresh.isRefreshing = false
                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    if (!isLoadingMoreItems && !binding.swipeRefresh.isRefreshing)
                        binding.clLoader.visible()
                }
            }
        })

    }

    fun clickItem(pos: Int) {
        val item = items[pos]

        when (item.pushType) {
            PushType.NEW_REQUEST, PushType.REQUEST_FAILED, PushType.REQUEST_COMPLETED, PushType.PATIENT_ADDED_SYMPTOMS,
            PushType.CANCELED_REQUEST, PushType.RESCHEDULED_REQUEST, PushType.UPCOMING_APPOINTMENT-> {
                openScreen(DrawerActivity.APPOINTMENT_DETAIL, item.module_id ?: "")
            }

            PushType.DOCUMENT_REJECTED -> {
                    startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                        .putExtra(SubCategoryFragment.CATEGORY_PARENT_ID, userRepository.getUser()?.categoryData)
                        .putExtra(DocumentsFragment.UPDATE_DOCUMENTS, true), AppRequestCode.PROFILE_UPDATE)
                }
            PushType.PROFILE_REJECTED -> {
                startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(UPDATE_CATEGORY, true), AppRequestCode.PROFILE_UPDATE)
            }
        }
    }

    private fun openScreen(page: String, id: String = "") {
        val intent = Intent(requireContext(), DrawerActivity::class.java)
            .putExtra(PAGE_TO_OPEN, page)

        if (id.isNotEmpty())
            intent.putExtra(EXTRA_REQUEST_ID, id)

        startActivity(intent)
    }
}
