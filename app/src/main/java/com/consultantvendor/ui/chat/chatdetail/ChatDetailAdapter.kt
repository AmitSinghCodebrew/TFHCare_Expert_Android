package com.consultantvendor.ui.chat.chatdetail


import android.app.Activity
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.chat.ChatMessage
import com.consultantvendor.data.network.Config
import com.consultantvendor.databinding.ItemChatLeftBinding
import com.consultantvendor.databinding.ItemChatRightBinding
import com.consultantvendor.databinding.ItemChatTypingBinding
import com.consultantvendor.utils.*
import com.consultantvendor.utils.DateUtils.dateFormatFromMillis
import java.util.*

class ChatDetailAdapter(private var context: ChatDetailActivity,
                        private var data: ArrayList<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val UI_LEFT = 0
        private const val UI_RIGHT = 1
        private const val TYPING = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            data[position].messageType == DocType.MESSAGE_TYPING -> {
                TYPING
            }
            data[position].receiverId == context.userRepository.getUser()?.id -> {
                UI_LEFT
            }
            else -> {
                UI_RIGHT
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): RecyclerView.ViewHolder {
        return when (position) {

            UI_RIGHT -> {
                ViewHolderRight(DataBindingUtil.inflate(LayoutInflater
                        .from(context), R.layout.item_chat_right, viewGroup, false))
            }
            UI_LEFT -> {
                ViewHolderLeft(DataBindingUtil.inflate(LayoutInflater
                        .from(context), R.layout.item_chat_left, viewGroup, false))
            }
            TYPING -> {
                ViewHolderType(DataBindingUtil.inflate(LayoutInflater
                        .from(context), R.layout.item_chat_typing, viewGroup, false))
            }
            else -> ViewHolderRight(DataBindingUtil.inflate(LayoutInflater
                    .from(context), R.layout.item_chat_right, viewGroup, false))
        }
    }

    inner class ViewHolderType(val binding:ItemChatTypingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {

        }
    }


    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val showDateHeader: Boolean
        if (position == data.size - 1) {
            showDateHeader = true
        } else {
            val cal1 = Calendar.getInstance()
            cal1.timeInMillis = data[position + 1].sentAt ?: 0
            val cal2 = Calendar.getInstance()
            cal2.timeInMillis = data[position].sentAt ?: 0
            showDateHeader = !(cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(
                    Calendar.DAY_OF_YEAR))
        }
        when (holder) {
            is ViewHolderRight -> holder.bind(data[position], showDateHeader)
            is ViewHolderLeft -> holder.bind(data[position], showDateHeader)
            is ViewHolderType -> holder.bind()
        }
    }


    inner class ViewHolderRight(val binding: ItemChatRightBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            /*Click*/
            binding.cvImageRight.setOnClickListener {
                val chat=data[adapterPosition]
                when (chat.messageType) {
                    DocType.IMAGE -> {
                        val itemImages = ArrayList<String>()
                        itemImages.add("${Config.imageURL}${ImageFolder.UPLOADS}${chat.imageUrl}")
                        viewImageFull(context as Activity, itemImages, 0)
                    }
                }
            }

            binding.tvPdfRight.setOnClickListener {
                val chat=data[adapterPosition]
                when (chat.messageType) {
                    DocType.PDF -> {
                        val link = "${Config.imageURL}${ImageFolder.PDF}${chat.imageUrl}"
                        openPdf(context, link)
                    }
                }
            }
        }

        fun bind(chat: ChatMessage, showDateHeader: Boolean) = with(binding) {
            tvDateRight.gone()
            tvTextRight.gone()
            cvImageRight.gone()
            tvPdfRight.gone()

            tvTimeRight.text = chat.sentAt?.let { DateUtils.formatDateTime(context, it, DateUtils.FORMAT_SHOW_TIME) }
            if (showDateHeader) {
                tvDateRight.visible()
                tvDateRight.text = chat.sentAt?.let { getDateHeader(it) }
            }

            when (chat.messageType) {
                DocType.TEXT -> {
                    tvTextRight.visible()
                    tvTextRight.text = chat.message
                }
                DocType.IMAGE -> {
                    cvImageRight.visible()
                    loadImage(ivImageRight, chat.imageUrl, R.drawable.image_placeholder)
                }
                DocType.PDF -> {
                    tvPdfRight.visible()
                    tvPdfRight.text=chat.imageUrl
                }
            }
            ivTick.setImageResource(getTickValue(chat.status))
        }
    }

    inner class ViewHolderLeft(val binding: ItemChatLeftBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            /*Click*/
            binding.cvImageLeft.setOnClickListener {
                val chat=data[adapterPosition]
                when (chat.messageType) {
                    DocType.IMAGE -> {
                        val itemImages = ArrayList<String>()
                        itemImages.add("${Config.imageURL}${ImageFolder.UPLOADS}${chat.imageUrl}")
                        viewImageFull(context as Activity, itemImages, 0)
                    }
                }
            }

            binding.tvPdfLeft.setOnClickListener {
                val chat=data[adapterPosition]
                when (chat.messageType) {
                    DocType.PDF -> {
                        val link = "${Config.imageURL}${ImageFolder.PDF}${chat.imageUrl}"
                        openPdf(context, link)
                    }
                }
            }
        }

        fun bind(chat: ChatMessage, showDateHeader: Boolean) = with(binding) {
            tvDateLeft.gone()
            tvTextLeft.gone()
            cvImageLeft.gone()
            tvPdfLeft.gone()

            tvTimeLeft.text = chat.sentAt?.let { DateUtils.formatDateTime(context, it, DateUtils.FORMAT_SHOW_TIME) }
            if (showDateHeader) {
                tvDateLeft.visible()
                tvDateLeft.text = chat.sentAt?.let { getDateHeader(it) }
            }


            when (chat.messageType) {
                DocType.TEXT -> {
                    tvTextLeft.visible()
                    tvTextLeft.text = chat.message
                }
                DocType.IMAGE -> {
                    cvImageLeft.visible()
                    loadImage(ivImageLeft, chat.imageUrl, R.drawable.image_placeholder)
                }
                DocType.PDF -> {
                    tvPdfLeft.visible()
                    tvPdfLeft.text=chat.imageUrl
                }
            }
        }
    }

    private fun getDateHeader(millis: Long): String? {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        val dateString: String?
        dateString = when {
            DateUtils.isToday(calendar.timeInMillis) -> context.getString(R.string.today)
            isYesterday(calendar) -> String.format("%s", context.getString(R.string.yesterday))
            else -> dateFormatFromMillis(DateFormat.DATE_FORMAT, calendar.timeInMillis)
        }
        return dateString
    }

    private fun getTickValue(status: String?): Int {
        return when (status) {
            AppSocket.MessageStatus.NOT_SENT -> R.drawable.ic_wait
            AppSocket.MessageStatus.SENT -> R.drawable.ic_sent
            AppSocket.MessageStatus.DELIVERED -> R.drawable.ic_delivered
            AppSocket.MessageStatus.SEEN -> R.drawable.ic_seen
            else -> R.drawable.ic_wait
        }
    }
}



